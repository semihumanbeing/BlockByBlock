package com.dahee.blockbyblock.domain.model

data class FoodBlockColorOption(
    val id: String,
    val nameKo: String,
    val nameEn: String,
    val hex: String
)

object FoodBlockPalette {
    val options = listOf(
        FoodBlockColorOption("red", "빨강", "Red", "#EA4832"),
        FoodBlockColorOption("orange", "주황", "Orange", "#FF7043"),
        FoodBlockColorOption("yellow", "노랑", "Yellow", "#FDD835"),
        FoodBlockColorOption("green", "초록", "Green", "#44923E"),
        FoodBlockColorOption("white", "흰색", "White", "#F4F5F3")
    )

    val defaultColorHex = "#FF7043" // Default Orange
}
