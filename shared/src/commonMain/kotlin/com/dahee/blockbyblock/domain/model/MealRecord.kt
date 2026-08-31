package com.dahee.blockbyblock.domain.model

enum class MealType(val title: String) {
    BREAKFAST("아침"),
    LUNCH("점심"),
    DINNER("저녁"),
    SNACK("간식")
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
 * Meal slot for a specific meal time (Breakfast, Lunch, Dinner, Snack).
 */
data class MealSlotRecord(
    val mealType: MealType,
    val blocks: List<MealBlockItem> = emptyList(),
    val memo: String = "",
    val customTitle: String = ""
)

/**
 * Full day meal record containing breakfast, lunch, dinner, snack.
 */
data class DayMealRecord(
    val id: String,
    val dateString: String, // YYYY-MM-DD format
    val breakfast: MealSlotRecord = MealSlotRecord(MealType.BREAKFAST),
    val lunch: MealSlotRecord = MealSlotRecord(MealType.LUNCH),
    val dinner: MealSlotRecord = MealSlotRecord(MealType.DINNER),
    val snack: MealSlotRecord = MealSlotRecord(MealType.SNACK),
    val updatedAt: Long = 0L
) {
    val totalBlockCount: Int
        get() = breakfast.blocks.size + lunch.blocks.size + dinner.blocks.size + snack.blocks.size

    fun getSlot(type: MealType): MealSlotRecord = when (type) {
        MealType.BREAKFAST -> breakfast
        MealType.LUNCH -> lunch
        MealType.DINNER -> dinner
        MealType.SNACK -> snack
    }

    fun updateSlot(slot: MealSlotRecord): DayMealRecord = when (slot.mealType) {
        MealType.BREAKFAST -> copy(breakfast = slot)
        MealType.LUNCH -> copy(lunch = slot)
        MealType.DINNER -> copy(dinner = slot)
        MealType.SNACK -> copy(snack = slot)
    }
}
