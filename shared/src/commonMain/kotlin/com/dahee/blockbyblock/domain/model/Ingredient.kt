package com.dahee.blockbyblock.domain.model

data class Ingredient(
    val id: String,
    val name: String,
    val status: IngredientStatus = IngredientStatus.STOCK,
    val category: IngredientCategory = IngredientCategory.OTHER,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
