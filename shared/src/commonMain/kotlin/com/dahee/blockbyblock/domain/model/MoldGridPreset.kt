package com.dahee.blockbyblock.domain.model

enum class MoldGridPreset(
    val capacityMl: Int,
    val rows: Int,
    val cols: Int,
    val label: String,
    val defaultCellCount: Int = 4
) {
    ML_500(500, 3, 3, "500ml", 2),
    ML_250(250, 2, 3, "250ml", 4),
    ML_125(125, 1, 3, "125ml", 6),
    ML_30(30, 1, 1, "30ml", 16),
    CUSTOM(-1, 2, 2, "Custom", 6);

    companion object {
        fun fromCapacity(capacity: Int): MoldGridPreset {
            return entries.find { it != CUSTOM && it.capacityMl == capacity } ?: CUSTOM
        }

        val defaultPresets: List<MoldGridPreset>
            get() = listOf(ML_500, ML_250, ML_125, ML_30)
    }
}
