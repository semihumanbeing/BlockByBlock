package com.dahee.blockbyblock.domain.model

data class CookingInstruction(
    val toolType: CookingToolType,
    val temperature: Int? = null,
    val timeMinutes: Int? = null,
    val timeSeconds: Int? = null
)

data class FoodBlock(
    val id: String,
    val name: String,
    val moldId: String,
    val moldName: String,
    val moldCapacityMl: Int,
    val moldCellCount: Int,
    val moldColorHex: String,
    val moldPreset: MoldGridPreset? = null,
    val blockColorHex: String = "#FF7043", // 3D Food Block Color
    val mainIngredients: List<String>,
    val subIngredients: List<String> = emptyList(),
    val quantity: Int = 1,
    val shelfLifeDays: Int = 90,
    val cookingInstructions: List<CookingInstruction> = emptyList(),
    val createdAt: Long = 0L,
    val memo: String = ""
) {
    val cookingToolType: CookingToolType?
        get() = cookingInstructions.firstOrNull()?.toolType

    val cookingTemperature: Int?
        get() = cookingInstructions.firstOrNull()?.temperature

    val cookingTimeMinutes: Int?
        get() = cookingInstructions.firstOrNull()?.timeMinutes

    val cookingTimeSeconds: Int?
        get() = cookingInstructions.firstOrNull()?.timeSeconds
}
