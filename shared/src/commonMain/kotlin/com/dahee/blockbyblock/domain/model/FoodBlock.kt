package com.dahee.blockbyblock.domain.model

enum class StorageType {
    FREEZER,
    FRIDGE
}

data class FoodBlock(
    val id: String,
    val name: String,
    val moldId: String,
    val moldName: String,
    val moldCapacityMl: Int,
    val moldCellCount: Int,
    val moldColorHex: String,
    val moldPreset: MoldGridPreset? = null,
    val blockColorHex: String = "#FF7043", // 3D Lego Food Block Color
    val mainIngredients: List<String>,
    val subIngredients: List<String> = emptyList(),
    val quantity: Int = 1,
    val storageType: StorageType = StorageType.FREEZER,
    val shelfLifeDays: Int = 90,
    val cookingToolType: CookingToolType? = null,
    val createdAt: Long = 0L,
    val memo: String = ""
)
