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
        get() = customCapacityMl ?: moldPreset?.capacityMl ?: 250

    val isDefaultName: Boolean
        get() = isDefaultMoldName(name, displayCapacity, customCapacityMl)

    companion object {
        private val AUTO_GENERATED_MOLD_REGEX = Regex(
            """^(((\d+(/\d+)?(\.\d+)?\s*(ml|Cup|Tbsp))|Custom)\s*)?[\(·/]?\s*(\d+\s*(칸|slots?|구|cell|cells)?)?\s*\)?$""",
            RegexOption.IGNORE_CASE
        )

        fun isDefaultMoldName(name: String, displayCapacity: Int = 0, customCapacityMl: Int? = null): Boolean {
            if (name.isBlank()) return true
            val trimmed = name.trim()
            if (AUTO_GENERATED_MOLD_REGEX.matches(trimmed)) return true

            val fallbackKoreanPattern = Regex("""^.*?\b\d+\s*칸\s*$""")
            val startsWithCapacity = (displayCapacity > 0 && trimmed.startsWith("${displayCapacity}ml", ignoreCase = true)) ||
                    (customCapacityMl != null && trimmed.startsWith("${customCapacityMl}ml", ignoreCase = true)) ||
                    trimmed.startsWith("Custom", ignoreCase = true)

            if (startsWithCapacity && fallbackKoreanPattern.matches(trimmed)) return true

            return false
        }
    }
}
