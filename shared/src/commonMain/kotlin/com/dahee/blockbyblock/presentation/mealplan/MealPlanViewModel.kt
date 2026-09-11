package com.dahee.blockbyblock.presentation.mealplan

import com.dahee.blockbyblock.core.utils.getCurrentDateIso
import com.dahee.blockbyblock.core.utils.getCurrentEpochMillis
import com.dahee.blockbyblock.domain.model.DayMealRecord
import com.dahee.blockbyblock.domain.model.FoodBlock
import com.dahee.blockbyblock.domain.model.MealBlockItem
import com.dahee.blockbyblock.domain.model.MealBlockStatus
import com.dahee.blockbyblock.domain.model.MealPreset
import com.dahee.blockbyblock.domain.model.MealSlotRecord
import com.dahee.blockbyblock.domain.model.MealType
import com.dahee.blockbyblock.domain.model.determineBlockStatusesIndexed
import com.dahee.blockbyblock.domain.repository.FoodBlockRepository
import com.dahee.blockbyblock.domain.repository.MealRecordRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class DateNavigationState(
    val selectedTab: MealPlanTab = MealPlanTab.TODAY,
    val selectedDateString: String = getCurrentDateIso(),
    val weekStartDate: String = MealPlanViewModel.getMondayOfWeek(getCurrentDateIso())
)

private data class SlotDialogInternalState(
    val isOpen: Boolean = false,
    val dateString: String = "",
    val dateLabel: String = "",
    val mealType: MealType = MealType.LUNCH,
    val selectedBlocks: List<MealBlockItem> = emptyList(),
    val availablePieces: List<AvailableBlockPiece> = emptyList(),
    val title: String = "",
    val memo: String = ""
)

class MealPlanViewModel(
    private val mealRecordRepository: MealRecordRepository,
    private val foodBlockRepository: FoodBlockRepository,
    private val viewModelScope: CoroutineScope = CoroutineScope(Dispatchers.Main),
    initialLanguage: com.dahee.blockbyblock.core.i18n.AppLanguage = com.dahee.blockbyblock.core.i18n.AppLanguage.KO
) {
    private val todayString: String
        get() = getCurrentDateIso()

    private val _navState = MutableStateFlow(DateNavigationState())
    private val _dialogState = MutableStateFlow(SlotDialogInternalState())
    private val _languageState = MutableStateFlow(initialLanguage)

    // In-memory cache for weekly meal records keyed by weekStartDate ("YYYY-MM-DD")
    private val weeklyMealCache = mutableMapOf<String, List<DayMealRecord>>()
    private val activePrefetchJobs = mutableMapOf<String, Job>()
    private var loadWeekJob: Job? = null

    fun setLanguage(language: com.dahee.blockbyblock.core.i18n.AppLanguage) {
        _languageState.value = language
    }

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<MealPlanUiState> = combine(
        mealRecordRepository.observeMealRecords(),
        foodBlockRepository.observeFoodBlocks(),
        mealRecordRepository.observeMealPresets(),
        _navState,
        _dialogState,
        _languageState
    ) { params ->
        val records = params[0] as List<DayMealRecord>
        val foodBlocks = params[1] as List<FoodBlock>
        val presets = params[2] as List<MealPreset>
        val nav = params[3] as DateNavigationState
        val dialog = params[4] as SlotDialogInternalState
        val lang = params[5] as com.dahee.blockbyblock.core.i18n.AppLanguage

        val currentDayRecord = records.find { it.dateString == nav.selectedDateString }

        val weekDates = generate7Days(nav.weekStartDate)
        val dayModels = weekDates.mapIndexed { index, dateStr ->
            val record = records.find { it.dateString == dateStr }
            val dayOfWeekNum = getDayOfWeekNumber(dateStr)
            val dayOfWeekName = getDayOfWeekName(dateStr, lang)
            val monthDay = formatMonthDay(dateStr)
            DayMealPlanUiModel(
                dateString = dateStr,
                dayOfWeekName = dayOfWeekName,
                monthDayDisplay = monthDay,
                isToday = dateStr == todayString,
                isSelected = dateStr == nav.selectedDateString,
                isSunday = dayOfWeekNum == 0,
                isSaturday = dayOfWeekNum == 6,
                mealRecord = record
            )
        }

        val weekLabel = computeWeekLabel(nav.weekStartDate, weekDates.last(), lang)
        val selectedDateFormatted = formatFullDate(nav.selectedDateString, lang)
        val dialogDateLabel = if (dialog.dateString.isNotBlank()) formatFullDate(dialog.dateString, lang) else dialog.dateLabel

        MealPlanUiState(
            selectedTab = nav.selectedTab,
            todayDateString = todayString,
            selectedDateString = nav.selectedDateString,
            selectedDateFormatted = selectedDateFormatted,
            isSelectedDateToday = nav.selectedDateString == todayString,
            currentDayMealRecord = currentDayRecord,
            currentWeekLabel = weekLabel,
            weekDays = dayModels,
            weekStartDateString = nav.weekStartDate,
            isSlotDialogOpen = dialog.isOpen,
            editingDateString = dialog.dateString,
            editingDateLabel = dialogDateLabel,
            editingMealType = dialog.mealType,
            slotSelectedBlocks = dialog.selectedBlocks,
            slotAvailableBlocks = dialog.availablePieces,
            slotTitleInput = dialog.title,
            slotMemoInput = dialog.memo,
            savedPresets = presets,
            allFoodBlocks = foodBlocks
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MealPlanUiState()
    )

    init {
        loadWeek(_navState.value.weekStartDate)
        viewModelScope.launch {
            mealRecordRepository.fetchMealPresets()
        }
    }

    fun loadWeek(startDate: String, forceRefresh: Boolean = false) {
        val prevWeek = offsetDate(startDate, -7)
        val nextWeek = offsetDate(startDate, 7)

        if (!forceRefresh && weeklyMealCache.containsKey(startDate)) {
            // Already cached: 0ms delay. Trigger background prefetch for adjacent weeks
            prefetchWeek(prevWeek)
            prefetchWeek(nextWeek)
            return
        }

        loadWeekJob?.cancel()
        loadWeekJob = viewModelScope.launch {
            val result = mealRecordRepository.fetchWeeklyMeals(startDate)
            result.onSuccess { records ->
                weeklyMealCache[startDate] = records
                // Background prefetch adjacent weeks
                prefetchWeek(prevWeek)
                prefetchWeek(nextWeek)
            }
        }
    }

    private fun prefetchWeek(startDate: String) {
        if (weeklyMealCache.containsKey(startDate)) return
        if (activePrefetchJobs[startDate]?.isActive == true) return

        val job = viewModelScope.launch {
            try {
                val result = mealRecordRepository.fetchWeeklyMeals(startDate)
                result.onSuccess { records ->
                    weeklyMealCache[startDate] = records
                }
            } catch (_: Exception) {
                // Silently ignore background prefetch errors
            } finally {
                activePrefetchJobs.remove(startDate)
            }
        }
        activePrefetchJobs[startDate] = job
    }

    fun isWeekCached(weekStartDate: String): Boolean = weeklyMealCache.containsKey(weekStartDate)

    fun invalidateWeekCache(weekStartDate: String? = null) {
        if (weekStartDate != null) {
            weeklyMealCache.remove(weekStartDate)
            loadWeek(weekStartDate, forceRefresh = true)
        } else {
            weeklyMealCache.clear()
            loadWeek(_navState.value.weekStartDate, forceRefresh = true)
        }
    }

    internal fun updateWeekCacheForDay(dayRecord: DayMealRecord) {
        val monday = getMondayOfWeek(dayRecord.dateString)
        val cached = weeklyMealCache[monday]
        if (cached != null) {
            val updated = cached.filterNot { it.dateString == dayRecord.dateString } + dayRecord
            weeklyMealCache[monday] = updated
        }
    }

    internal fun removeDayFromWeekCache(dateString: String) {
        val monday = getMondayOfWeek(dateString)
        val cached = weeklyMealCache[monday]
        if (cached != null) {
            weeklyMealCache[monday] = cached.filterNot { it.dateString == dateString }
        }
    }

    fun onSelectTab(tab: MealPlanTab) {
        _navState.value = _navState.value.copy(selectedTab = tab)
    }

    fun onPreviousDay() {
        val prevDate = offsetDate(_navState.value.selectedDateString, -1)
        val targetWeek = getMondayOfWeek(prevDate)
        _navState.value = _navState.value.copy(
            selectedDateString = prevDate,
            weekStartDate = targetWeek
        )
        loadWeek(targetWeek)
    }

    fun onNextDay() {
        val nextDate = offsetDate(_navState.value.selectedDateString, 1)
        val targetWeek = getMondayOfWeek(nextDate)
        _navState.value = _navState.value.copy(
            selectedDateString = nextDate,
            weekStartDate = targetWeek
        )
        loadWeek(targetWeek)
    }

    fun onSelectDateAndOpenDayView(dateString: String) {
        val targetWeek = getMondayOfWeek(dateString)
        _navState.value = _navState.value.copy(
            selectedDateString = dateString,
            weekStartDate = targetWeek,
            selectedTab = MealPlanTab.TODAY
        )
        loadWeek(targetWeek)
    }

    fun onResetToToday() {
        val today = getCurrentDateIso()
        val currentMonday = getMondayOfWeek(today)
        _navState.value = _navState.value.copy(
            selectedDateString = today,
            weekStartDate = currentMonday,
            selectedTab = MealPlanTab.TODAY
        )
        loadWeek(currentMonday)
    }

    fun onPreviousWeek() {
        val prevWeek = offsetDate(_navState.value.weekStartDate, -7)
        _navState.value = _navState.value.copy(weekStartDate = prevWeek)
        loadWeek(prevWeek)
    }

    fun onNextWeek() {
        val nextWeek = offsetDate(_navState.value.weekStartDate, 7)
        _navState.value = _navState.value.copy(weekStartDate = nextWeek)
        loadWeek(nextWeek)
    }

    fun onCurrentWeek() {
        val today = getCurrentDateIso()
        val currentMonday = getMondayOfWeek(today)
        _navState.value = _navState.value.copy(weekStartDate = currentMonday)
        loadWeek(currentMonday)
    }

    fun onOpenSlotDialog(dateString: String, dateLabel: String, mealType: MealType) {
        viewModelScope.launch {
            val record = mealRecordRepository.getMealRecordByDate(dateString)
            val slot = record?.getSlot(mealType) ?: MealSlotRecord(mealType)

            val currentSelected = slot.blocks

            // Generate full piece pool from food blocks storage (multiple identical blocks appear individually)
            val allFoodBlocks = foodBlockRepository.getFoodBlocks()
            val allPieces = mutableListOf<AvailableBlockPiece>()

            allFoodBlocks.forEach { block ->
                if (block.quantity > 0) {
                    for (i in 1..block.quantity) {
                        allPieces.add(
                            AvailableBlockPiece(
                                instanceId = "${block.id}-piece-$i",
                                blockId = block.id,
                                blockName = block.name,
                                blockColorHex = block.blockColorHex,
                                moldCapacityMl = block.moldCapacityMl,
                                moldCellCount = block.moldCellCount
                            )
                        )
                    }
                }
            }

            // Remove already selected count for each block from available pieces
            val availableList = allPieces.toMutableList()
            currentSelected.forEach { sel ->
                val matchIdx = availableList.indexOfFirst { it.blockId == sel.blockId }
                if (matchIdx >= 0) {
                    availableList.removeAt(matchIdx)
                }
            }

            val initialTitle = if (slot.customTitle.isNotBlank()) slot.customTitle else ""

            _dialogState.value = SlotDialogInternalState(
                isOpen = true,
                dateString = dateString,
                dateLabel = dateLabel,
                mealType = mealType,
                selectedBlocks = currentSelected,
                availablePieces = availableList,
                title = initialTitle,
                memo = slot.memo
            )
        }
    }

    fun onCloseSlotDialog() {
        _dialogState.value = SlotDialogInternalState()
    }

    fun onMoveBlockToTop(piece: AvailableBlockPiece) {
        val currentSelected = _dialogState.value.selectedBlocks.toMutableList()
        val currentAvailable = _dialogState.value.availablePieces.toMutableList()

        val itemToAdd = MealBlockItem(
            instanceId = piece.instanceId,
            blockId = piece.blockId,
            blockName = piece.blockName,
            blockColorHex = piece.blockColorHex,
            moldCapacityMl = piece.moldCapacityMl,
            moldCellCount = piece.moldCellCount,
            sortOrder = currentSelected.size
        )

        currentSelected.add(itemToAdd)
        currentAvailable.remove(piece)

        _dialogState.value = _dialogState.value.copy(
            selectedBlocks = currentSelected,
            availablePieces = currentAvailable
        )
    }

    fun onMoveBlockToBottom(item: MealBlockItem) {
        val currentSelected = _dialogState.value.selectedBlocks.toMutableList()
        val currentAvailable = _dialogState.value.availablePieces.toMutableList()

        currentSelected.remove(item)
        currentAvailable.add(
            AvailableBlockPiece(
                instanceId = item.instanceId,
                blockId = item.blockId,
                blockName = item.blockName,
                blockColorHex = item.blockColorHex,
                moldCapacityMl = item.moldCapacityMl,
                moldCellCount = item.moldCellCount
            )
        )

        val reindexedSelected = currentSelected.mapIndexed { index, b -> b.copy(sortOrder = index) }

        _dialogState.value = _dialogState.value.copy(
            selectedBlocks = reindexedSelected,
            availablePieces = currentAvailable
        )
    }

    fun onReorderSelectedBlocks(fromIndex: Int, toIndex: Int) {
        val currentList = _dialogState.value.selectedBlocks.toMutableList()
        if (fromIndex in currentList.indices && toIndex in currentList.indices) {
            val moved = currentList.removeAt(fromIndex)
            currentList.add(toIndex, moved)
            _dialogState.value = _dialogState.value.copy(
                selectedBlocks = currentList.mapIndexed { index, b -> b.copy(sortOrder = index) }
            )
        }
    }

    fun onTitleInputChange(newTitle: String) {
        // Enforce maximum 50 characters
        if (newTitle.length <= 50) {
            _dialogState.value = _dialogState.value.copy(title = newTitle)
        }
    }

    fun onMemoInputChange(newMemo: String) {
        _dialogState.value = _dialogState.value.copy(memo = newMemo)
    }

    var onSlotSavedListener: (() -> Unit)? = null
    var onSaveErrorListener: ((String) -> Unit)? = null

    fun onSaveSlot() {
        val dialog = _dialogState.value
        if (dialog.selectedBlocks.isEmpty()) {
            return
        }
        val currentFoodBlocks = uiState.value.allFoodBlocks
        val statuses = determineBlockStatusesIndexed(dialog.selectedBlocks, currentFoodBlocks)
        if (statuses.any { it != MealBlockStatus.AVAILABLE }) {
            return
        }
        viewModelScope.launch {
            try {
                val existingDay = mealRecordRepository.getMealRecordByDate(dialog.dateString)
                    ?: DayMealRecord(
                        id = "meal-${dialog.dateString}",
                        dateString = dialog.dateString
                    )

                val customTitleToSave = if (dialog.mealType == MealType.SNACK || dialog.mealType == MealType.EXTRA) {
                    dialog.title.trim().ifBlank { if (dialog.mealType == MealType.SNACK) "간식" else "추가" }
                } else {
                    dialog.title.trim()
                }

                val updatedSlot = MealSlotRecord(
                    mealType = dialog.mealType,
                    blocks = dialog.selectedBlocks.mapIndexed { index, b -> b.copy(sortOrder = index) },
                    memo = dialog.memo.trim(),
                    customTitle = customTitleToSave
                )

                val updatedDay = existingDay.updateSlot(updatedSlot).copy(updatedAt = currentTimeMillis())
                mealRecordRepository.saveMealRecord(updatedDay)
                updateWeekCacheForDay(updatedDay)
                onCloseSlotDialog()
                onSlotSavedListener?.invoke()
            } catch (e: Exception) {
                val msg = e.message ?: "식단 저장에 실패했습니다."
                onSaveErrorListener?.invoke(msg)
            }
        }
    }

    fun onDeleteSlot(dateString: String, mealType: MealType) {
        viewModelScope.launch {
            val existingDay = mealRecordRepository.getMealRecordByDate(dateString) ?: return@launch
            val emptySlot = MealSlotRecord(mealType = mealType, blocks = emptyList(), memo = "")
            val updatedDay = existingDay.updateSlot(emptySlot).copy(updatedAt = currentTimeMillis())
            if (updatedDay.totalBlockCount == 0 &&
                updatedDay.breakfast.memo.isBlank() &&
                updatedDay.lunch.memo.isBlank() &&
                updatedDay.dinner.memo.isBlank() &&
                updatedDay.snack.memo.isBlank() &&
                updatedDay.extra.memo.isBlank()
            ) {
                mealRecordRepository.deleteMealRecord(updatedDay.id)
                removeDayFromWeekCache(updatedDay.dateString)
            } else {
                mealRecordRepository.saveMealRecord(updatedDay)
                updateWeekCacheForDay(updatedDay)
            }
        }
    }

    fun onSaveCurrentAsPreset(presetName: String) {
        val dialog = _dialogState.value
        if (dialog.selectedBlocks.isEmpty()) return
        val currentFoodBlocks = uiState.value.allFoodBlocks
        val statuses = determineBlockStatusesIndexed(dialog.selectedBlocks, currentFoodBlocks)
        if (statuses.any { it != MealBlockStatus.AVAILABLE }) return

        val defaultName = if (dialog.title.isNotBlank()) dialog.title.trim()
        else dialog.selectedBlocks.joinToString(" + ") { it.blockName }
        val finalName = presetName.trim().ifBlank { defaultName }

        viewModelScope.launch {
            try {
                val newPreset = MealPreset(
                    id = "preset-${currentTimeMillis()}",
                    name = finalName,
                    blocks = dialog.selectedBlocks.mapIndexed { index, b -> b.copy(sortOrder = index) },
                    memo = dialog.memo.trim(),
                    createdAt = currentTimeMillis()
                )
                mealRecordRepository.saveMealPreset(newPreset)
            } catch (e: Exception) {
                val msg = e.message ?: "프리셋 저장에 실패했습니다."
                onSaveErrorListener?.invoke(msg)
            }
        }
    }

    fun onSaveSlotAsPreset(dateString: String, mealType: MealType, presetName: String? = null) {
        viewModelScope.launch {
            try {
                val dayRecord = mealRecordRepository.getMealRecordByDate(dateString) ?: return@launch
                val slot = dayRecord.getSlot(mealType)
                if (slot.blocks.isEmpty()) return@launch
                val currentFoodBlocks = uiState.value.allFoodBlocks
                val statuses = determineBlockStatusesIndexed(slot.blocks, currentFoodBlocks)
                if (statuses.any { it != MealBlockStatus.AVAILABLE }) return@launch

                val defaultName = if (slot.customTitle.isNotBlank()) slot.customTitle
                else "${slot.mealType.title} 식단 (${slot.blocks.joinToString(", ") { it.blockName }})"
                val finalName = presetName?.trim()?.ifBlank { defaultName } ?: defaultName

                val newPreset = MealPreset(
                    id = "preset-${currentTimeMillis()}",
                    name = finalName,
                    blocks = slot.blocks.mapIndexed { index, b -> b.copy(sortOrder = index) },
                    memo = slot.memo,
                    createdAt = currentTimeMillis()
                )
                mealRecordRepository.saveMealPreset(newPreset)
            } catch (e: Exception) {
                val msg = e.message ?: "프리셋 저장에 실패했습니다."
                onSaveErrorListener?.invoke(msg)
            }
        }
    }

    fun onApplyPreset(preset: MealPreset) {
        viewModelScope.launch {
            val allFoodBlocks = foodBlockRepository.getFoodBlocks()
            val allPieces = mutableListOf<AvailableBlockPiece>()
            allFoodBlocks.forEach { block ->
                if (block.quantity > 0) {
                    for (i in 1..block.quantity) {
                        allPieces.add(
                            AvailableBlockPiece(
                                instanceId = "${block.id}-piece-$i",
                                blockId = block.id,
                                blockName = block.name,
                                blockColorHex = block.blockColorHex,
                                moldCapacityMl = block.moldCapacityMl,
                                moldCellCount = block.moldCellCount
                            )
                        )
                    }
                }
            }

            val newSelected = preset.blocks.mapIndexed { index, blockItem ->
                blockItem.copy(
                    instanceId = "${blockItem.blockId}-preset-${currentTimeMillis()}-$index",
                    sortOrder = index
                )
            }

            val availableList = allPieces.toMutableList()
            newSelected.forEach { sel ->
                val matchIdx = availableList.indexOfFirst { it.blockId == sel.blockId }
                if (matchIdx >= 0) {
                    availableList.removeAt(matchIdx)
                }
            }

            _dialogState.value = _dialogState.value.copy(
                selectedBlocks = newSelected,
                availablePieces = availableList,
                title = if (_dialogState.value.title.isBlank()) preset.name else _dialogState.value.title,
                memo = if (_dialogState.value.memo.isBlank()) preset.memo else _dialogState.value.memo
            )
        }
    }

    fun onRefillBlockQuantity(blockId: String, delta: Int = 1) {
        viewModelScope.launch {
            foodBlockRepository.updateQuantity(blockId, delta)
            val currentDialog = _dialogState.value
            if (currentDialog.isOpen) {
                val updatedFoodBlocks = foodBlockRepository.getFoodBlocks()
                val allPieces = mutableListOf<AvailableBlockPiece>()
                updatedFoodBlocks.forEach { block ->
                    if (block.quantity > 0) {
                        for (i in 1..block.quantity) {
                            allPieces.add(
                                AvailableBlockPiece(
                                    instanceId = "${block.id}-piece-$i",
                                    blockId = block.id,
                                    blockName = block.name,
                                    blockColorHex = block.blockColorHex,
                                    moldCapacityMl = block.moldCapacityMl,
                                    moldCellCount = block.moldCellCount
                                )
                            )
                        }
                    }
                }
                val availableList = allPieces.toMutableList()
                currentDialog.selectedBlocks.forEach { sel ->
                    val matchIdx = availableList.indexOfFirst { it.blockId == sel.blockId }
                    if (matchIdx >= 0) {
                        availableList.removeAt(matchIdx)
                    }
                }
                _dialogState.value = currentDialog.copy(availablePieces = availableList)
            }
        }
    }

    fun onDeletePreset(presetId: String) {
        viewModelScope.launch {
            mealRecordRepository.deleteMealPreset(presetId)
        }
    }

    companion object {
        private fun generate7Days(startMonday: String): List<String> {
            return (0..6).map { offsetDate(startMonday, it) }
        }

        private fun formatMonthDay(dateStr: String): String {
            val parts = dateStr.split("-")
            if (parts.size == 3) {
                val m = parts[1].toIntOrNull() ?: 1
                val d = parts[2].toIntOrNull() ?: 1
                return "$m.$d"
            }
            return dateStr
        }

        fun getDayOfWeekNumber(dateStr: String): Int {
            val parts = dateStr.split("-")
            if (parts.size != 3) return 0
            val y = parts[0].toIntOrNull() ?: 2026
            val m = parts[1].toIntOrNull() ?: 8
            val d = parts[2].toIntOrNull() ?: 30

            val t = intArrayOf(0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4)
            var yearAdj = y
            if (m < 3) yearAdj -= 1
            return (yearAdj + yearAdj / 4 - yearAdj / 100 + yearAdj / 400 + t[m - 1] + d) % 7
        }

        fun getDayOfWeekKorean(dateStr: String): String {
            return when (getDayOfWeekNumber(dateStr)) {
                0 -> "일"
                1 -> "월"
                2 -> "화"
                3 -> "수"
                4 -> "목"
                5 -> "금"
                6 -> "토"
                else -> "일"
            }
        }

        fun getDayOfWeekEnglish(dateStr: String): String {
            return when (getDayOfWeekNumber(dateStr)) {
                0 -> "Sun"
                1 -> "Mon"
                2 -> "Tue"
                3 -> "Wed"
                4 -> "Thu"
                5 -> "Fri"
                6 -> "Sat"
                else -> "Sun"
            }
        }

        fun getDayOfWeekName(
            dateStr: String,
            lang: com.dahee.blockbyblock.core.i18n.AppLanguage = com.dahee.blockbyblock.core.i18n.AppLanguage.KO
        ): String {
            return if (lang == com.dahee.blockbyblock.core.i18n.AppLanguage.EN) {
                getDayOfWeekEnglish(dateStr)
            } else {
                getDayOfWeekKorean(dateStr)
            }
        }

        private fun getEnglishMonthShort(month: Int): String {
            return when (month) {
                1 -> "Jan"
                2 -> "Feb"
                3 -> "Mar"
                4 -> "Apr"
                5 -> "May"
                6 -> "Jun"
                7 -> "Jul"
                8 -> "Aug"
                9 -> "Sep"
                10 -> "Oct"
                11 -> "Nov"
                12 -> "Dec"
                else -> "Jan"
            }
        }

        fun formatFullDate(
            dateStr: String,
            lang: com.dahee.blockbyblock.core.i18n.AppLanguage = com.dahee.blockbyblock.core.i18n.AppLanguage.KO
        ): String {
            val parts = dateStr.split("-")
            if (parts.size != 3) return dateStr
            val y = parts[0]
            val m = parts[1].toIntOrNull() ?: 1
            val d = parts[2].toIntOrNull() ?: 1
            val dayName = getDayOfWeekName(dateStr, lang)

            return if (lang == com.dahee.blockbyblock.core.i18n.AppLanguage.EN) {
                val monthStr = getEnglishMonthShort(m)
                "$dayName, $monthStr $d, $y"
            } else {
                "${y}년 ${m}월 ${d}일 ($dayName)"
            }
        }

        fun formatFullDateKorean(dateStr: String): String = formatFullDate(dateStr, com.dahee.blockbyblock.core.i18n.AppLanguage.KO)

        private fun computeWeekLabel(
            startMonday: String,
            endSunday: String,
            lang: com.dahee.blockbyblock.core.i18n.AppLanguage = com.dahee.blockbyblock.core.i18n.AppLanguage.KO
        ): String {
            val startParts = startMonday.split("-")
            val endParts = endSunday.split("-")
            if (startParts.size == 3 && endParts.size == 3) {
                val y = startParts[0]
                val sm = startParts[1].toIntOrNull() ?: 1
                val sd = startParts[2].toIntOrNull() ?: 1
                val em = endParts[1].toIntOrNull() ?: 1
                val ed = endParts[2].toIntOrNull() ?: 1

                val weekOfMonth = ((sd - 1) / 7) + 1
                return if (lang == com.dahee.blockbyblock.core.i18n.AppLanguage.EN) {
                    val monthStr = getEnglishMonthShort(sm)
                    "Week $weekOfMonth, $monthStr $y (${sm}.${sd} ~ ${em}.${ed})"
                } else {
                    "${y}년 ${sm}월 ${weekOfMonth}주차 (${sm}.${sd} ~ ${em}.${ed})"
                }
            }
            return "$startMonday ~ $endSunday"
        }

        fun offsetDate(baseDate: String, days: Int): String {
            val parts = baseDate.split("-")
            if (parts.size != 3) return baseDate
            var y = parts[0].toIntOrNull() ?: 2026
            var m = parts[1].toIntOrNull() ?: 8
            var d = parts[2].toIntOrNull() ?: 30

            d += days

            while (d < 1) {
                m -= 1
                if (m < 1) {
                    m = 12
                    y -= 1
                }
                d += daysInMonth(y, m)
            }

            while (d > daysInMonth(y, m)) {
                d -= daysInMonth(y, m)
                m += 1
                if (m > 12) {
                    m = 1
                    y += 1
                }
            }

            val mm = if (m < 10) "0$m" else "$m"
            val dd = if (d < 10) "0$d" else "$d"
            return "$y-$mm-$dd"
        }

        private fun daysInMonth(year: Int, month: Int): Int {
            return when (month) {
                1, 3, 5, 7, 8, 10, 12 -> 31
                4, 6, 9, 11 -> 30
                2 -> if (isLeapYear(year)) 29 else 28
                else -> 30
            }
        }

        private fun isLeapYear(year: Int): Boolean {
            return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
        }

        fun getMondayOfWeek(dateStr: String): String {
            val dayNum = getDayOfWeekNumber(dateStr)
            val daysToSubtract = if (dayNum == 0) 6 else dayNum - 1
            return offsetDate(dateStr, -daysToSubtract)
        }

        private fun currentTimeMillis(): Long {
            return getCurrentEpochMillis()
        }
    }
}
