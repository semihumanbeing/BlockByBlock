package com.dahee.blockbyblock.domain.model

enum class MoldGridPreset(
    val capacityMl: Int,
    val rows: Int,
    val cols: Int,
    val label: String
) {
    ML_500(500, 3, 3, "500ml"),
    ML_250(250, 2, 3, "250ml"),
    ML_125(125, 1, 3, "125ml"),
    ML_75(75, 1, 1, "75ml"),
    CUSTOM(-1, 2, 2, "직접 입력");

    companion object {
        fun fromCapacity(capacity: Int): MoldGridPreset {
            return entries.find { it != CUSTOM && it.capacityMl == capacity } ?: CUSTOM
        }

        val defaultPresets: List<MoldGridPreset>
            get() = listOf(ML_500, ML_250, ML_125, ML_75)
    }
}
