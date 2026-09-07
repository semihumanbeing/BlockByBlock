package com.dahee.blockbyblock.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CatalogIngredientResponse(
    val id: Long,
    val name: String,
    val category: String
)

@Serializable
data class IngredientCountsResponse(
    val stock: Int = 0,
    val outOfStock: Int = 0,
    val cart: Int = 0
)

@Serializable
data class IngredientResponse(
    val id: Long,
    val name: String,
    val status: String,
    val category: String,
    val updatedAt: String? = null
)

@Serializable
data class IngredientListResponse(
    val ingredients: List<IngredientResponse>,
    val counts: IngredientCountsResponse
)

@Serializable
data class CreateIngredientRequest(
    val name: String,
    val status: String,
    val category: String
)

@Serializable
data class UpdateIngredientRequest(
    val name: String,
    val category: String
)

@Serializable
data class UpdateIngredientStatusRequest(
    val status: String
)
