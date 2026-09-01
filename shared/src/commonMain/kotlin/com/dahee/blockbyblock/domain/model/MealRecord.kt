package com.dahee.blockbyblock.domain.model

enum class MealType(val title: String) {
    BREAKFAST("아침"),
    LUNCH("점심"),
    DINNER("저녁"),
    SNACK("간식"),
    EXTRA("추가")
}

/**
 * Single Food Block item recorded in a meal.
 */
data class MealBlockItem(
    val instanceId: String, // Unique per individual block instance in the tray
    val blockId: String,   // Source FoodBlock ID
    val blockName: String,
    val blockColorHex: String = "#FF7043",
    val moldCapacityMl: Int = 250,
    val moldCellCount: Int = 4
)

/**
 * Meal slot for a specific meal time (Breakfast, Lunch, Dinner, Snack, Extra).
 */
data class MealSlotRecord(
    val mealType: MealType,
    val blocks: List<MealBlockItem> = emptyList(),
    val memo: String = "",
    val customTitle: String = ""
)

/**
 * Full day meal record containing breakfast, lunch, dinner, snack, extra (up to 5 meals).
 */
data class DayMealRecord(
    val id: String,
    val dateString: String, // YYYY-MM-DD format
    val breakfast: MealSlotRecord = MealSlotRecord(MealType.BREAKFAST),
    val lunch: MealSlotRecord = MealSlotRecord(MealType.LUNCH),
    val dinner: MealSlotRecord = MealSlotRecord(MealType.DINNER),
    val snack: MealSlotRecord = MealSlotRecord(MealType.SNACK),
    val extra: MealSlotRecord = MealSlotRecord(MealType.EXTRA),
    val updatedAt: Long = 0L
) {
    val totalBlockCount: Int
        get() = breakfast.blocks.size + lunch.blocks.size + dinner.blocks.size + snack.blocks.size + extra.blocks.size

    fun getSlot(type: MealType): MealSlotRecord = when (type) {
        MealType.BREAKFAST -> breakfast
        MealType.LUNCH -> lunch
        MealType.DINNER -> dinner
        MealType.SNACK -> snack
        MealType.EXTRA -> extra
    }

    fun updateSlot(slot: MealSlotRecord): DayMealRecord = when (slot.mealType) {
        MealType.BREAKFAST -> copy(breakfast = slot)
        MealType.LUNCH -> copy(lunch = slot)
        MealType.DINNER -> copy(dinner = slot)
        MealType.SNACK -> copy(snack = slot)
        MealType.EXTRA -> copy(extra = slot)
    }
}

/**
 * Saved reusable meal preset / combination (식단 프리셋 / 세트).
 */
data class MealPreset(
    val id: String,
    val name: String,
    val blocks: List<MealBlockItem> = emptyList(),
    val memo: String = "",
    val createdAt: Long = 0L
)

