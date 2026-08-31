package com.dahee.blockbyblock.presentation.mealplan

import com.dahee.blockbyblock.domain.model.DayMealRecord
import com.dahee.blockbyblock.domain.model.FoodBlock
import com.dahee.blockbyblock.domain.model.MealBlockItem
import com.dahee.blockbyblock.domain.model.MealPreset
import com.dahee.blockbyblock.domain.model.MealSlotRecord
import com.dahee.blockbyblock.domain.model.MealType
import com.dahee.blockbyblock.domain.repository.FoodBlockRepository
import com.dahee.blockbyblock.domain.repository.MealRecordRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class DateNavigationState(
    val selectedTab: MealPlanTab = MealPlanTab.TODAY,
    val selectedDateString: String = "2026-08-30",
    val weekStartDate: String = "2026-08-24"
)

private data class SlotDialogInternalState(
    val isOpen: Boolean = false,
    val dateString: String = "2026-08-30",
    val dateLabel: String = "8월 30일 (일)",
    val mealType: MealType = MealType.LUNCH,
    val selectedBlocks: List<MealBlockItem> = emptyList(),
    val availablePieces: List<AvailableBlockPiece> = emptyList(),
    val title: String = "",
    val memo: String = ""
)

class MealPlanViewModel(
    private val mealRecordRepository: MealRecordRepository,
    private val foodBlockRepository: FoodBlockRepository,
    private val viewModelScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val todayString = "2026-08-30"

    private val _navState = MutableStateFlow(DateNavigationState())
    private val _dialogState = MutableStateFlow(SlotDialogInternalState())

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<MealPlanUiState> = combine(
        mealRecordRepository.observeMealRecords(),
        foodBlockRepository.observeFoodBlocks(),
        mealRecordRepository.observeMealPresets(),
        _navState,
        _dialogState
    ) { params ->
        val records = params[0] as List<DayMealRecord>
        val foodBlocks = params[1] as List<FoodBlock>
        val presets = params[2] as List<MealPreset>
        val nav = params[3] as DateNavigationState
        val dialog = params[4] as SlotDialogInternalState

        val currentDayRecord = records.find { it.dateString == nav.selectedDateString }

        val weekDates = generate7Days(nav.weekStartDate)
        val dayModels = weekDates.mapIndexed { index, dateStr ->
            val record = records.find { it.dateString == dateStr }
            val dayOfWeekKorean = getDayOfWeekKorean(dateStr)
            val monthDay = formatMonthDay(dateStr)
            DayMealPlanUiModel(
                dateString = dateStr,
                dayOfWeekName = dayOfWeekKorean,
                monthDayDisplay = monthDay,
                isToday = dateStr == todayString,
                isSelected = dateStr == nav.selectedDateString,
                mealRecord = record
            )
        }

        val weekLabel = computeWeekLabel(nav.weekStartDate, weekDates.last())
        val selectedDateFormatted = formatFullDateKorean(nav.selectedDateString)

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
            editingDateLabel = dialog.dateLabel,
            editingMealType = dialog.mealType,
            slotSelectedBlocks = dialog.selectedBlocks,
            slotAvailableBlocks = dialog.availablePieces,
            slotTitleInput = dialog.title,
            slotMemoInput = dialog.memo,
            savedPresets = presets
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MealPlanUiState()
    )

    fun onSelectTab(tab: MealPlanTab) {
        _navState.value = _navState.value.copy(selectedTab = tab)
    }

    fun onPreviousDay() {
        val prevDate = offsetDate(_navState.value.selectedDateString, -1)
        _navState.value = _navState.value.copy(selectedDateString = prevDate)
    }

    fun onNextDay() {
        val nextDate = offsetDate(_navState.value.selectedDateString, 1)
        _navState.value = _navState.value.copy(selectedDateString = nextDate)
    }

    fun onSelectDateAndOpenDayView(dateString: String) {
        _navState.value = _navState.value.copy(
            selectedDateString = dateString,
            selectedTab = MealPlanTab.TODAY
        )
    }

    fun onResetToToday() {
        _navState.value = _navState.value.copy(
            selectedDateString = todayString,
            selectedTab = MealPlanTab.TODAY
        )
    }

    fun onPreviousWeek() {
        val prevWeek = offsetDate(_navState.value.weekStartDate, -7)
        _navState.value = _navState.value.copy(weekStartDate = prevWeek)
    }

    fun onNextWeek() {
        val nextWeek = offsetDate(_navState.value.weekStartDate, 7)
        _navState.value = _navState.value.copy(weekStartDate = nextWeek)
    }

    fun onCurrentWeek() {
        _navState.value = _navState.value.copy(weekStartDate = "2026-08-24")
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
                for (i in 1..block.quantity.coerceAtLeast(1)) {
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
            moldCellCount = piece.moldCellCount
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

        _dialogState.value = _dialogState.value.copy(
            selectedBlocks = currentSelected,
            availablePieces = currentAvailable
        )
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

    fun onSaveSlot() {
        val dialog = _dialogState.value
        if (dialog.selectedBlocks.isEmpty()) {
            return
        }
        viewModelScope.launch {
            val existingDay = mealRecordRepository.getMealRecordByDate(dialog.dateString)
                ?: DayMealRecord(
                    id = "meal-${dialog.dateString}",
                    dateString = dialog.dateString
                )

            val customTitleToSave = if (dialog.mealType == MealType.SNACK) {
                dialog.title.trim().ifBlank { "간식" }
            } else {
                dialog.title.trim()
            }

            val updatedSlot = MealSlotRecord(
                mealType = dialog.mealType,
                blocks = dialog.selectedBlocks,
                memo = dialog.memo.trim(),
                customTitle = customTitleToSave
            )

            val updatedDay = existingDay.updateSlot(updatedSlot).copy(updatedAt = currentTimeMillis())
            mealRecordRepository.saveMealRecord(updatedDay)
            onCloseSlotDialog()
            onSlotSavedListener?.invoke()
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
                updatedDay.snack.memo.isBlank()
            ) {
                mealRecordRepository.deleteMealRecord(updatedDay.id)
            } else {
                mealRecordRepository.saveMealRecord(updatedDay)
            }
        }
    }

    fun onSaveCurrentAsPreset(presetName: String) {
        val dialog = _dialogState.value
        if (dialog.selectedBlocks.isEmpty()) return
        val defaultName = if (dialog.title.isNotBlank()) dialog.title.trim()
        else dialog.selectedBlocks.joinToString(" + ") { it.blockName }
        val finalName = presetName.trim().ifBlank { defaultName }

        viewModelScope.launch {
            val newPreset = MealPreset(
                id = "preset-${currentTimeMillis()}",
                name = finalName,
                blocks = dialog.selectedBlocks,
                memo = dialog.memo.trim(),
                createdAt = currentTimeMillis()
            )
            mealRecordRepository.saveMealPreset(newPreset)
        }
    }

    fun onSaveSlotAsPreset(dateString: String, mealType: MealType, presetName: String? = null) {
        viewModelScope.launch {
            val dayRecord = mealRecordRepository.getMealRecordByDate(dateString) ?: return@launch
            val slot = dayRecord.getSlot(mealType)
            if (slot.blocks.isEmpty()) return@launch
            val defaultName = if (slot.customTitle.isNotBlank()) slot.customTitle
            else "${slot.mealType.title} 식단 (${slot.blocks.joinToString(", ") { it.blockName }})"
            val finalName = presetName?.trim()?.ifBlank { defaultName } ?: defaultName

            val newPreset = MealPreset(
                id = "preset-${currentTimeMillis()}",
                name = finalName,
                blocks = slot.blocks,
                memo = slot.memo,
                createdAt = currentTimeMillis()
            )
            mealRecordRepository.saveMealPreset(newPreset)
        }
    }

    fun onApplyPreset(preset: MealPreset) {
        viewModelScope.launch {
            val allFoodBlocks = foodBlockRepository.getFoodBlocks()
            val allPieces = mutableListOf<AvailableBlockPiece>()
            allFoodBlocks.forEach { block ->
                for (i in 1..block.quantity.coerceAtLeast(1)) {
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

            val newSelected = preset.blocks.mapIndexed { index, blockItem ->
                blockItem.copy(instanceId = "${blockItem.blockId}-preset-${currentTimeMillis()}-$index")
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

        fun getDayOfWeekKorean(dateStr: String): String {
            val parts = dateStr.split("-")
            if (parts.size != 3) return "일"
            val y = parts[0].toIntOrNull() ?: 2026
            val m = parts[1].toIntOrNull() ?: 8
            val d = parts[2].toIntOrNull() ?: 30

            // Zeller-like calculation for Day of Week (0 = Sun, 1 = Mon, ... 6 = Sat)
            val t = intArrayOf(0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4)
            var yearAdj = y
            if (m < 3) yearAdj -= 1
            val dayOfWeekNum = (yearAdj + yearAdj / 4 - yearAdj / 100 + yearAdj / 400 + t[m - 1] + d) % 7

            return when (dayOfWeekNum) {
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

        fun formatFullDateKorean(dateStr: String): String {
            val parts = dateStr.split("-")
            if (parts.size != 3) return dateStr
            val y = parts[0]
            val m = parts[1].toIntOrNull() ?: 1
            val d = parts[2].toIntOrNull() ?: 1
            val dayName = getDayOfWeekKorean(dateStr)
            return "${y}년 ${m}월 ${d}일 ($dayName)"
        }

        private fun computeWeekLabel(startMonday: String, endSunday: String): String {
            val startParts = startMonday.split("-")
            val endParts = endSunday.split("-")
            if (startParts.size == 3 && endParts.size == 3) {
                val y = startParts[0]
                val sm = startParts[1].toIntOrNull() ?: 1
                val sd = startParts[2].toIntOrNull() ?: 1
                val em = endParts[1].toIntOrNull() ?: 1
                val ed = endParts[2].toIntOrNull() ?: 1

                val weekOfMonth = ((sd - 1) / 7) + 1
                return "${y}년 ${sm}월 ${weekOfMonth}주차 (${sm}.${sd} ~ ${em}.${ed})"
            }
            return "$startMonday ~ $endSunday"
        }

        private fun offsetDate(baseDate: String, days: Int): String {
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

        private fun currentTimeMillis(): Long {
            return 1756540800000L
        }
    }
}
