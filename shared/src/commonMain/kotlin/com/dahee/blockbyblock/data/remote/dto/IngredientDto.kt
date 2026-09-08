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
data class IngredientPageResponse(
    val items: List<IngredientResponse> = emptyList(),
    val page: Int = 1,
    val size: Int = 12,
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false
)

@Serializable
data class IngredientListResponse(
    val ingredients: List<IngredientResponse> = emptyList(),
    val page: IngredientPageResponse? = null,
    val counts: IngredientCountsResponse = IngredientCountsResponse()
) {
    val allIngredients: List<IngredientResponse>
        get() = page?.items ?: ingredients

    val resolvedPage: IngredientPageResponse
        get() = page ?: IngredientPageResponse(
            items = ingredients,
            page = 1,
            size = ingredients.size.coerceAtLeast(12),
            totalElements = ingredients.size.toLong(),
            totalPages = 1,
            hasNext = false,
            hasPrevious = false
        )
}

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
