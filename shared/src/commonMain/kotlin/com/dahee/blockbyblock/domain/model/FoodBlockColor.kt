package com.dahee.blockbyblock.domain.model

data class FoodBlockColorOption(
    val id: String,
    val nameKo: String,
    val nameEn: String,
    val hex: String
)

enum class FoodBlockColorType(val suffix: String) {
    RED("red"),
    ORANGE("orange"),
    YELLOW("yellow"),
    GREEN("green"),
    BROWN("brown"),
    WHITE("white");

    companion object {
        fun fromHex(colorHex: String): FoodBlockColorType {
            val cleanHex = colorHex.uppercase().trim()
            return when {
                cleanHex.contains("EA4832") || cleanHex.contains("EF4444") || cleanHex.contains("F87171") || cleanHex.contains("DC2626") -> RED
                cleanHex.contains("FDD835") || cleanHex.contains("EAB308") || cleanHex.contains("FDE047") || cleanHex.contains("FEF08A") -> YELLOW
                cleanHex.contains("44923E") || cleanHex.contains("22C55E") || cleanHex.contains("4ADE80") || cleanHex.contains("16A34A") || cleanHex.contains("A7F3D0") -> GREEN
                cleanHex.contains("633B10") || cleanHex.contains("8B4513") || cleanHex.contains("78350F") || cleanHex.contains("92400E") || cleanHex.contains("5C2C0C") -> BROWN
                cleanHex.contains("F4F5F3") || cleanHex.contains("F1F5F9") || cleanHex.contains("FFFFFF") || cleanHex.contains("E2E8F0") -> WHITE
                else -> ORANGE
            }
        }
    }
}

object FoodBlockPalette {
    val options = listOf(
        FoodBlockColorOption("red", "빨강", "Red", "#EA4832"),
        FoodBlockColorOption("orange", "주황", "Orange", "#FF7043"),
        FoodBlockColorOption("yellow", "노랑", "Yellow", "#FDD835"),
        FoodBlockColorOption("green", "초록", "Green", "#44923E"),
        FoodBlockColorOption("brown", "갈색", "Brown", "#633B10"),
        FoodBlockColorOption("white", "흰색", "White", "#F4F5F3")
    )

    val defaultColorHex = "#FF7043" // Default Orange
}
