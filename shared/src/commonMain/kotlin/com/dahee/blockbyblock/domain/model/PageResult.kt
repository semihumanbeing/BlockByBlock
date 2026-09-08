package com.dahee.blockbyblock.domain.model

data class PageResult<T>(
    val items: List<T> = emptyList(),
    val page: Int = 1,
    val size: Int = 12,
    val totalElements: Long = 0L,
    val totalPages: Int = 0,
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false
)

data class IngredientCounts(
    val stock: Int = 0,
    val outOfStock: Int = 0,
    val cart: Int = 0
) {
    val total: Int get() = stock + outOfStock + cart
}

data class IngredientPagedResult(
    val page: PageResult<Ingredient>,
    val counts: IngredientCounts = IngredientCounts()
)
