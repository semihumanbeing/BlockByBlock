package com.dahee.blockbyblock.data.repository

import com.dahee.blockbyblock.domain.model.Ingredient
import com.dahee.blockbyblock.domain.model.IngredientCategory
import com.dahee.blockbyblock.domain.model.IngredientStatus
import com.dahee.blockbyblock.domain.repository.IngredientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class InMemoryIngredientRepository : IngredientRepository {

    // Clean initial state with no cluttering sample data
    private val _ingredients = MutableStateFlow<List<Ingredient>>(emptyList())

    override fun getAllIngredients(): Flow<List<Ingredient>> {
        return _ingredients.asStateFlow()
    }

    override fun searchIngredients(
        query: String,
        statusFilter: IngredientStatus?,
        categoryFilter: IngredientCategory?
    ): Flow<List<Ingredient>> {
        return _ingredients.map { list ->
            list.filter { ingredient ->
                val matchesQuery = query.isBlank() ||
                        ingredient.name.contains(query, ignoreCase = true) ||
                        ingredient.category.displayNameKo.contains(query, ignoreCase = true) ||
                        ingredient.category.displayNameEn.contains(query, ignoreCase = true)

                val matchesStatus = statusFilter == null || ingredient.status == statusFilter
                val matchesCategory = categoryFilter == null || ingredient.category == categoryFilter

                matchesQuery && matchesStatus && matchesCategory
            }
        }
    }

    override fun getSearchSuggestions(query: String): List<Ingredient> {
        if (query.isBlank()) return emptyList()
        return _ingredients.value.filter {
            it.name.contains(query, ignoreCase = true)
        }.take(5)
    }

    override suspend fun upsertIngredient(ingredient: Ingredient) {
        val currentList = _ingredients.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == ingredient.id }
        if (index >= 0) {
            currentList[index] = ingredient
        } else {
            currentList.add(0, ingredient)
        }
        _ingredients.value = currentList
    }

    override suspend fun updateStatus(id: String, status: IngredientStatus) {
        val currentList = _ingredients.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index >= 0) {
            val item = currentList[index]
            currentList[index] = item.copy(status = status)
            _ingredients.value = currentList
        }
    }

    override suspend fun toggleStatus(id: String) {
        val currentList = _ingredients.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index >= 0) {
            val item = currentList[index]
            val newStatus = when (item.status) {
                IngredientStatus.STOCK -> IngredientStatus.OUT_OF_STOCK
                IngredientStatus.OUT_OF_STOCK -> IngredientStatus.CART
                IngredientStatus.CART -> IngredientStatus.STOCK
            }
            currentList[index] = item.copy(status = newStatus)
            _ingredients.value = currentList
        }
    }

    override suspend fun deleteIngredient(id: String) {
        val currentList = _ingredients.value.toMutableList()
        currentList.removeAll { it.id == id }
        _ingredients.value = currentList
    }

    override suspend fun getIngredientById(id: String): Ingredient? {
        return _ingredients.value.find { it.id == id }
    }

    override suspend fun fetchIngredients(): Result<List<Ingredient>> {
        return Result.success(_ingredients.value)
    }

    override suspend fun fetchIngredientsPaged(
        page: Int,
        size: Int,
        status: IngredientStatus?,
        category: IngredientCategory?,
        query: String?
    ): Result<com.dahee.blockbyblock.domain.model.IngredientPagedResult> {
        val filtered = _ingredients.value.filter { item ->
            val matchesQuery = query.isNullOrBlank() ||
                    item.name.contains(query, ignoreCase = true) ||
                    item.category.displayNameKo.contains(query, ignoreCase = true) ||
                    item.category.displayNameEn.contains(query, ignoreCase = true)
            val matchesStatus = status == null || item.status == status
            val matchesCategory = category == null || item.category == category
            matchesQuery && matchesStatus && matchesCategory
        }

        val totalElements = filtered.size
        val totalPages = if (totalElements == 0) 0 else ((totalElements - 1) / size) + 1
        val startIndex = ((page - 1) * size).coerceIn(0, totalElements)
        val endIndex = (startIndex + size).coerceAtMost(totalElements)
        val pagedItems = if (startIndex < totalElements) filtered.subList(startIndex, endIndex) else emptyList()

        val counts = com.dahee.blockbyblock.domain.model.IngredientCounts(
            stock = _ingredients.value.count { it.status == IngredientStatus.STOCK },
            outOfStock = _ingredients.value.count { it.status == IngredientStatus.OUT_OF_STOCK },
            cart = _ingredients.value.count { it.status == IngredientStatus.CART }
        )

        return Result.success(
            com.dahee.blockbyblock.domain.model.IngredientPagedResult(
                page = com.dahee.blockbyblock.domain.model.PageResult(
                    items = pagedItems,
                    page = page,
                    size = size,
                    totalElements = totalElements.toLong(),
                    totalPages = totalPages,
                    hasNext = page < totalPages,
                    hasPrevious = page > 1
                ),
                counts = counts
            )
        )
    }

    override suspend fun fetchCatalogIngredients(
        query: String?,
        category: IngredientCategory?,
        lang: String
    ): Result<List<com.dahee.blockbyblock.domain.model.CatalogIngredient>> {
        val results = com.dahee.blockbyblock.data.datasource.MasterIngredientCatalog.search(query ?: "", category, lang)
        return Result.success(results)
    }
}
