package com.dahee.blockbyblock.domain.model

data class CatalogIngredient(
    val id: String,
    val name: String,
    val defaultUnit: IngredientUnit = IngredientUnit.GRAM,
    val defaultQuantity: Double = 100.0,
    val category: IngredientCategory = IngredientCategory.OTHER
)
