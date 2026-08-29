package com.dahee.blockbyblock.domain.model

data class Equipment(
    val id: String,
    val name: String,
    val category: EquipmentCategory,
    // [Mold specific attributes]
    val moldPreset: MoldGridPreset? = null,
    val customCapacityMl: Int? = null,
    val cellCount: Int = 1,                 // Total compartments per mold (e.g. 1, 4, 6 slots)
    val moldColorHex: String = "#BAE6FD",   // Silicone pastel color hex
    val quantity: Int = 1,                  // Number of units owned
    // [Cooking tool specific attributes]
    val toolType: CookingToolType? = null,
    val memo: String = "",
    val isPreset: Boolean = false,
    val isOwned: Boolean = true,
    val createdAt: Long = 0L
) {
    val displayCapacity: Int
        get() = if (moldPreset == MoldGridPreset.CUSTOM) {
            customCapacityMl ?: 0
        } else {
            moldPreset?.capacityMl ?: 0
        }

    val gridRows: Int
        get() = when (moldPreset) {
            MoldGridPreset.ML_500 -> 3
            MoldGridPreset.ML_250 -> 2
            MoldGridPreset.ML_125 -> 1
            MoldGridPreset.ML_75 -> 1
            MoldGridPreset.CUSTOM -> 2
            null -> 1
        }

    val gridCols: Int
        get() = when (moldPreset) {
            MoldGridPreset.ML_500 -> 3
            MoldGridPreset.ML_250 -> 3
            MoldGridPreset.ML_125 -> 3
            MoldGridPreset.ML_75 -> 1
            MoldGridPreset.CUSTOM -> 2
            null -> 1
        }
}
