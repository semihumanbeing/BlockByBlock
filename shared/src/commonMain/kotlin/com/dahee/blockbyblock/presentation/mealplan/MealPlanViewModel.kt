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
    val originalBlocks: List<MealBlockItem> = emptyList(),
    val selectedBlocks: List<MealBlockItem> = emptyList(),
    val availablePieces: List<AvailableBlockPiece> = emptyList(),
    val title: String = "",
    val memo: String = "",
    val pendingRefillCounts: Map<String, Int> = emptyMap()
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

    private var latestRawFoodBlocks: List<FoodBlock> = emptyList()

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
        latestRawFoodBlocks = foodBlocks
        val presets = params[2] as List<MealPreset>
        val nav = params[3] as DateNavigationState
        val dialog = params[4] as SlotDialogInternalState
        val lang = params[5] as com.dahee.blockbyblock.core.i18n.AppLanguage

        val effectiveFoodBlocks = if (dialog.isOpen && dialog.pendingRefillCounts.isNotEmpty()) {
            foodBlocks.map { fb ->
                val pending = dialog.pendingRefillCounts[fb.id] ?: 0
                if (pending > 0) fb.copy(quantity = fb.quantity + pending) else fb
            }
        } else {
            foodBlocks
        }

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
            slotOriginalBlocks = dialog.originalBlocks,
            slotSelectedBlocks = dialog.selectedBlocks,
            slotAvailableBlocks = dialog.availablePieces,
            slotTitleInput = dialog.title,
            slotMemoInput = dialog.memo,
            savedPresets = presets,
            allFoodBlocks = effectiveFoodBlocks,
            hasPendingRefills = dialog.pendingRefillCounts.isNotEmpty()
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
            foodBlockRepository.fetchFoodBlocks()
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

    fun onSelectDate(dateString: String) {
        val targetWeek = getMondayOfWeek(dateString)
        _navState.value = _navState.value.copy(
            selectedDateString = dateString,
            weekStartDate = targetWeek
        )
        loadWeek(targetWeek)
    }

    fun onSelectWeek(startDate: String) {
        val targetWeek = getMondayOfWeek(startDate)
        val weekDates = generate7Days(targetWeek)
        val today = todayString
        val newSelectedDate = if (weekDates.contains(today)) {
            today
        } else if (weekDates.contains(_navState.value.selectedDateString)) {
            _navState.value.selectedDateString
        } else {
            targetWeek
        }
        _navState.value = _navState.value.copy(
            weekStartDate = targetWeek,
            selectedDateString = newSelectedDate
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
            foodBlockRepository.fetchFoodBlocks()
            val record = mealRecordRepository.getMealRecordByDate(dateString)
            val slot = record?.getSlot(mealType) ?: MealSlotRecord(mealType)

            val currentSelected = slot.blocks

            // Generate full piece pool from current freezer food blocks storage
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

            val initialTitle = if (slot.customTitle.isNotBlank()) slot.customTitle else ""

            _dialogState.value = SlotDialogInternalState(
                isOpen = true,
                dateString = dateString,
                dateLabel = dateLabel,
                mealType = mealType,
                originalBlocks = currentSelected,
                selectedBlocks = currentSelected,
                availablePieces = allPieces,
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
        val index = currentSelected.indexOf(item)
        if (index == -1) return

        val currentFoodBlocks = uiState.value.allFoodBlocks
        val statuses = determineBlockStatusesIndexed(
            currentSelected,
            currentFoodBlocks,
            _dialogState.value.originalBlocks
        )
        val status = statuses.getOrElse(index) { MealBlockStatus.AVAILABLE }

        val countInSelectedBefore = currentSelected.count { it.blockId == item.blockId }
        val rawStock = (latestRawFoodBlocks.find { it.id == item.blockId }?.quantity ?: 0) +
            _dialogState.value.originalBlocks.count { it.blockId == item.blockId }

        currentSelected.removeAt(index)

        // Only return to available pieces if backed by actual inventory
        if (status == MealBlockStatus.AVAILABLE && countInSelectedBefore <= rawStock) {
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
        }

        val reindexedSelected = currentSelected.mapIndexed { i, b -> b.copy(sortOrder = i) }

        val newPending = _dialogState.value.pendingRefillCounts.toMutableMap()
        if (countInSelectedBefore > rawStock && (newPending[item.blockId] ?: 0) > 0) {
            val currentPending = newPending[item.blockId] ?: 1
            if (currentPending <= 1) {
                newPending.remove(item.blockId)
            } else {
                newPending[item.blockId] = currentPending - 1
            }
        }

        _dialogState.value = _dialogState.value.copy(
            selectedBlocks = reindexedSelected,
            availablePieces = currentAvailable,
            pendingRefillCounts = newPending
        )
    }

    fun onRemoveInvalidBlocks() {
        val currentSelected = _dialogState.value.selectedBlocks.toMutableList()
        val currentFoodBlocks = uiState.value.allFoodBlocks
        val statuses = determineBlockStatusesIndexed(
            currentSelected,
            currentFoodBlocks,
            _dialogState.value.originalBlocks
        )

        val validBlocks = currentSelected.filterIndexed { index, _ ->
            statuses.getOrElse(index) { MealBlockStatus.AVAILABLE } == MealBlockStatus.AVAILABLE
        }

        val reindexedSelected = validBlocks.mapIndexed { i, b -> b.copy(sortOrder = i) }

        _dialogState.value = _dialogState.value.copy(
            selectedBlocks = reindexedSelected
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
        val statuses = determineBlockStatusesIndexed(dialog.selectedBlocks, currentFoodBlocks, dialog.originalBlocks)
        if (statuses.any { it != MealBlockStatus.AVAILABLE }) {
            return
        }
        viewModelScope.launch {
            try {
                // Actually commit pending refills to repository now that user confirmed save
                if (dialog.pendingRefillCounts.isNotEmpty()) {
                    val rawFoodBlocks = latestRawFoodBlocks.associate { it.id to it.quantity }
                    val origCounts = dialog.originalBlocks.groupingBy { it.blockId }.eachCount()
                    val selectedCounts = dialog.selectedBlocks.groupingBy { it.blockId }.eachCount()

                    dialog.pendingRefillCounts.forEach { (blockId, count) ->
                        val inStock = rawFoodBlocks[blockId] ?: 0
                        val orig = origCounts[blockId] ?: 0
                        val selected = selectedCounts[blockId] ?: 0
                        val effectiveStock = inStock + orig
                        val missing = (selected - effectiveStock).coerceAtLeast(0)
                        val toRefill = minOf(count, missing)
                        if (toRefill > 0) {
                            foodBlockRepository.updateQuantity(blockId, toRefill)
                        }
                    }
                }

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
                foodBlockRepository.fetchFoodBlocks()
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
            foodBlockRepository.fetchFoodBlocks()
        }
    }

    fun onSaveCurrentAsPreset(presetName: String) {
        val dialog = _dialogState.value
        if (dialog.selectedBlocks.isEmpty()) return
        val currentFoodBlocks = uiState.value.allFoodBlocks
        val statuses = determineBlockStatusesIndexed(dialog.selectedBlocks, currentFoodBlocks, dialog.originalBlocks)
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
                val statuses = determineBlockStatusesIndexed(slot.blocks, currentFoodBlocks, slot.blocks)
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
            foodBlockRepository.fetchFoodBlocks()
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

            // Also credit original blocks from this slot since preset replaces them
            val origBlocks = _dialogState.value.originalBlocks
            origBlocks.forEachIndexed { idx, orig ->
                allPieces.add(
                    AvailableBlockPiece(
                        instanceId = "${orig.blockId}-orig-$idx",
                        blockId = orig.blockId,
                        blockName = orig.blockName,
                        blockColorHex = orig.blockColorHex,
                        moldCapacityMl = orig.moldCapacityMl,
                        moldCellCount = orig.moldCellCount
                    )
                )
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
                memo = if (_dialogState.value.memo.isBlank()) preset.memo else _dialogState.value.memo,
                pendingRefillCounts = emptyMap()
            )
        }
    }

    private suspend fun syncDialogAvailablePieces() {
        val currentDialog = _dialogState.value
        if (!currentDialog.isOpen) return
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
        // Subtract newly added blocks (selectedBlocks minus originalBlocks)
        val newlyAdded = currentDialog.selectedBlocks.toMutableList()
        currentDialog.originalBlocks.forEach { orig ->
            val idx = newlyAdded.indexOfFirst { it.blockId == orig.blockId }
            if (idx >= 0) newlyAdded.removeAt(idx)
        }
        val availableList = allPieces.toMutableList()
        newlyAdded.forEach { added ->
            val matchIdx = availableList.indexOfFirst { it.blockId == added.blockId }
            if (matchIdx >= 0) {
                availableList.removeAt(matchIdx)
            }
        }
        _dialogState.value = currentDialog.copy(availablePieces = availableList)
    }

    fun onRefillBlockQuantity(blockId: String, delta: Int = 1) {
        val dialog = _dialogState.value
        val newPending = dialog.pendingRefillCounts.toMutableMap()
        val nextVal = (newPending[blockId] ?: 0) + delta
        if (nextVal <= 0) {
            newPending.remove(blockId)
        } else {
            newPending[blockId] = nextVal
        }
        _dialogState.value = dialog.copy(pendingRefillCounts = newPending)
    }

    fun onRefillMissingBlocks() {
        val dialog = _dialogState.value
        if (!dialog.isOpen) return
        val currentFoodBlocks = uiState.value.allFoodBlocks.takeIf { it.isNotEmpty() }
            ?: return
        val statuses = determineBlockStatusesIndexed(
            dialog.selectedBlocks,
            currentFoodBlocks,
            dialog.originalBlocks
        )

        // Find all blocks that are OUT_OF_STOCK and calculate how many units need to be replenished
        val missingCounts = dialog.selectedBlocks
            .filterIndexed { index, _ -> statuses.getOrElse(index) { MealBlockStatus.AVAILABLE } == MealBlockStatus.OUT_OF_STOCK }
            .groupingBy { it.blockId }
            .eachCount()

        if (missingCounts.isEmpty()) return

        val newPending = dialog.pendingRefillCounts.toMutableMap()
        missingCounts.forEach { (blockId, count) ->
            newPending[blockId] = (newPending[blockId] ?: 0) + count
        }

        _dialogState.value = dialog.copy(pendingRefillCounts = newPending)
    }

    fun onDeletePreset(presetId: String) {
        viewModelScope.launch {
            mealRecordRepository.deleteMealPreset(presetId)
        }
    }

    companion object {
        fun generate7Days(startMonday: String): List<String> {
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

        fun getEnglishMonthShort(month: Int): String {
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

        fun computeWeekLabel(
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

        fun daysInMonth(year: Int, month: Int): Int {
            return when (month) {
                1, 3, 5, 7, 8, 10, 12 -> 31
                4, 6, 9, 11 -> 30
                2 -> if (isLeapYear(year)) 29 else 28
                else -> 30
            }
        }

        fun isLeapYear(year: Int): Boolean {
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
