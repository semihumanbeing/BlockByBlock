package com.dahee.blockbyblock.presentation.mealplan

import com.dahee.blockbyblock.domain.model.DayMealRecord
import com.dahee.blockbyblock.domain.model.FoodBlock
import com.dahee.blockbyblock.domain.model.MealBlockItem
import com.dahee.blockbyblock.domain.model.MealType

enum class MealPlanTab(val title: String) {
    TODAY("일별 식단"),
    WEEK("이번 주")
}

/**
 * Single block item available in storage tray for picking.
 */
data class AvailableBlockPiece(
    val instanceId: String,
    val blockId: String,
    val blockName: String,
    val blockColorHex: String,
    val moldCapacityMl: Int,
    val moldCellCount: Int
)

/**
 * UI representation of a single day in the weekly meal plan (Mon - Sun).
 */
data class DayMealPlanUiModel(
    val dateString: String,      // "2026-08-30"
    val dayOfWeekName: String,   // "일", "월", etc.
    val monthDayDisplay: String, // "8.30"
    val isToday: Boolean,
    val isSelected: Boolean,
    val mealRecord: DayMealRecord?
)

/**
 * UI State for MealPlanScreen and MealRecordDialog.
 */
data class MealPlanUiState(
    val selectedTab: MealPlanTab = MealPlanTab.TODAY,
    val todayDateString: String = "2026-08-30",

    // Day Tab Data (Supports navigating to yesterday, tomorrow, and any date)
    val selectedDateString: String = "2026-08-30",
    val selectedDateFormatted: String = "2026년 8월 30일 (일)",
    val isSelectedDateToday: Boolean = true,
    val currentDayMealRecord: DayMealRecord? = null,

    // Week Tab Data
    val currentWeekLabel: String = "", // e.g. "2026년 8월 5주차 (8.24 ~ 8.30)"
    val weekDays: List<DayMealPlanUiModel> = emptyList(),
    val weekStartDateString: String = "2026-08-24",

    // Meal Slot Edit Dialog State
    val isSlotDialogOpen: Boolean = false,
    val editingDateString: String = "2026-08-30",
    val editingDateLabel: String = "8월 30일 (일)",
    val editingMealType: MealType = MealType.LUNCH,
    val slotSelectedBlocks: List<MealBlockItem> = emptyList(), // Blocks currently placed in the slot (Top)
    val slotAvailableBlocks: List<AvailableBlockPiece> = emptyList(), // Blocks available to pick (Bottom)
    val slotTitleInput: String = "",
    val slotMemoInput: String = ""
)
