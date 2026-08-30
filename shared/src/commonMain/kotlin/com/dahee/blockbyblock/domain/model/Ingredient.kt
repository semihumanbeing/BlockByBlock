package com.dahee.blockbyblock.domain.model

data class Ingredient(
    val id: String,
    val name: String,
    val quantity: Double,
    val unit: IngredientUnit = IngredientUnit.GRAM,
    val status: IngredientStatus = IngredientStatus.IN_STOCK,
    val category: IngredientCategory = IngredientCategory.OTHER,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
) {
    val displayQuantity: String
        get() {
            return if (quantity % 1.0 == 0.0) {
                "${quantity.toInt()}${unit.symbol}"
            } else {
                "${quantity}${unit.symbol}"
            }
        }
}
