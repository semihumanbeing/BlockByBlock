package com.dahee.blockbyblock.domain.model

data class CatalogIngredient(
    val id: String,
    val name: String,
    val category: IngredientCategory = IngredientCategory.OTHER
)
