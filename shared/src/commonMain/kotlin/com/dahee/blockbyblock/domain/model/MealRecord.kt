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
    val moldCellCount: Int = 4,
    val sortOrder: Int = 0,
    val currentStock: Int? = null,
    val isDeleted: Boolean? = null,
    val blockStatus: MealBlockStatus? = null
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

/**
 * Availability status of a food block placed in a meal plan or preset.
 * Matches backend blockStatus ("AVAILABLE" | "OUT_OF_STOCK" | "DELETED").
 */
enum class MealBlockStatus {
    AVAILABLE,
    OUT_OF_STOCK,
    DELETED;

    companion object {
        fun from(status: String?): MealBlockStatus? =
            entries.find { it.name.equals(status, ignoreCase = true) }
    }
}

/**
 * Calculates the status of each block in a list according to current food block inventory.
 * Accurately tracks instance count so if a block has 1 in stock, the 2nd instance is marked OUT_OF_STOCK.
 */
fun determineBlockStatusesIndexed(
    blocks: List<MealBlockItem>,
    allFoodBlocks: List<FoodBlock>?,
    originalSlotBlocks: List<MealBlockItem> = emptyList()
): List<MealBlockStatus> {
    if (allFoodBlocks == null) {
        return blocks.map { item ->
            when {
                item.isDeleted == true || item.blockStatus == MealBlockStatus.DELETED -> MealBlockStatus.DELETED
                item.blockStatus != null -> item.blockStatus
                item.currentStock != null && item.currentStock <= 0 -> MealBlockStatus.OUT_OF_STOCK
                else -> MealBlockStatus.AVAILABLE
            }
        }
    }

    val foodBlocksMap = allFoodBlocks.associateBy { it.id }
    // Effective available stock = current freezer stock + blocks already allocated to this slot (excluding already out-of-stock/deleted ones)
    val remainingStock = allFoodBlocks.associate { it.id to it.quantity }.toMutableMap()
    originalSlotBlocks.forEach { orig ->
        if (orig.blockStatus != MealBlockStatus.OUT_OF_STOCK && orig.blockStatus != MealBlockStatus.DELETED && orig.isDeleted != true) {
            remainingStock[orig.blockId] = (remainingStock[orig.blockId] ?: 0) + 1
        }
    }

    return blocks.map { item ->
        val foodBlock = foodBlocksMap[item.blockId]
        when {
            item.isDeleted == true || item.blockStatus == MealBlockStatus.DELETED -> MealBlockStatus.DELETED
            foodBlock == null -> {
                // If not in active inventory and not explicitly deleted, it is OUT_OF_STOCK (depleted)
                MealBlockStatus.OUT_OF_STOCK
            }
            item.blockStatus == MealBlockStatus.OUT_OF_STOCK && foodBlock.quantity <= 0 -> {
                MealBlockStatus.OUT_OF_STOCK
            }
            else -> {
                val stock = remainingStock[item.blockId] ?: 0
                if (stock <= 0) {
                    MealBlockStatus.OUT_OF_STOCK
                } else {
                    remainingStock[item.blockId] = stock - 1
                    MealBlockStatus.AVAILABLE
                }
            }
        }
    }
}

