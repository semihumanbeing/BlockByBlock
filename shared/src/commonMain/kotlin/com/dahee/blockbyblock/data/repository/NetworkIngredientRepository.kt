package com.dahee.blockbyblock.data.repository

import com.dahee.blockbyblock.core.utils.getCurrentEpochMillis
import com.dahee.blockbyblock.data.remote.dto.CreateIngredientRequest
import com.dahee.blockbyblock.data.remote.dto.IngredientResponse
import com.dahee.blockbyblock.data.remote.dto.UpdateIngredientRequest
import com.dahee.blockbyblock.data.remote.service.IngredientApiService
import com.dahee.blockbyblock.domain.model.Ingredient
import com.dahee.blockbyblock.domain.model.IngredientCategory
import com.dahee.blockbyblock.domain.model.IngredientStatus
import com.dahee.blockbyblock.domain.repository.IngredientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class NetworkIngredientRepository(
    private val apiService: IngredientApiService = IngredientApiService(),
    initialItems: List<Ingredient> = emptyList()
) : IngredientRepository {

    private val _ingredients = MutableStateFlow(initialItems)

    override fun getAllIngredients(): Flow<List<Ingredient>> = _ingredients.asStateFlow()

    override suspend fun fetchIngredients(): Result<List<Ingredient>> {
        return apiService.getIngredients().map { response ->
            val mapped = response.allIngredients.map { mapResponseToIngredient(it) }
            _ingredients.value = mapped
            mapped
        }
    }

    override suspend fun fetchIngredientsPaged(
        page: Int,
        size: Int,
        status: IngredientStatus?,
        category: IngredientCategory?,
        query: String?
    ): Result<com.dahee.blockbyblock.domain.model.IngredientPagedResult> {
        return apiService.getIngredients(
            page = page,
            size = size,
            status = status?.name,
            category = category?.name,
            query = query
        ).map { response ->
            val pageResp = response.resolvedPage
            val mappedItems = pageResp.items.map { mapResponseToIngredient(it) }

            // Merge into local _ingredients so other views retain observed items
            _ingredients.update { current ->
                val newIds = mappedItems.map { it.id }.toSet()
                mappedItems + current.filterNot { newIds.contains(it.id) }
            }

            com.dahee.blockbyblock.domain.model.IngredientPagedResult(
                page = com.dahee.blockbyblock.domain.model.PageResult(
                    items = mappedItems,
                    page = pageResp.page,
                    size = pageResp.size,
                    totalElements = pageResp.totalElements,
                    totalPages = pageResp.totalPages,
                    hasNext = pageResp.hasNext,
                    hasPrevious = pageResp.hasPrevious
                ),
                counts = com.dahee.blockbyblock.domain.model.IngredientCounts(
                    stock = response.counts.stock,
                    outOfStock = response.counts.outOfStock,
                    cart = response.counts.cart
                )
            )
        }
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
        val numericId = ingredient.id.toLongOrNull()
        if (numericId != null) {
            val req = UpdateIngredientRequest(
                name = ingredient.name,
                category = ingredient.category.name
            )
            val result = apiService.updateIngredient(numericId, req)
            result.onSuccess { res ->
                val updated = mapResponseToIngredient(res)
                _ingredients.update { list -> list.map { if (it.id == ingredient.id) updated else it } }
            }.onFailure {
                _ingredients.update { list -> list.map { if (it.id == ingredient.id) ingredient else it } }
            }
        } else {
            val req = CreateIngredientRequest(
                name = ingredient.name,
                status = ingredient.status.name,
                category = ingredient.category.name
            )
            val result = apiService.createIngredient(req)
            result.onSuccess { res ->
                val created = mapResponseToIngredient(res)
                _ingredients.update { listOf(created) + it }
            }.onFailure {
                _ingredients.update { listOf(ingredient) + it }
            }
        }
    }

    override suspend fun updateStatus(id: String, status: IngredientStatus) {
        val numericId = id.toLongOrNull()
        if (numericId != null) {
            val result = apiService.updateStatus(numericId, status.name)
            result.onSuccess { res ->
                val updated = mapResponseToIngredient(res)
                _ingredients.update { list -> list.map { if (it.id == id) updated else it } }
                return
            }
        }
        _ingredients.update { list ->
            list.map { if (it.id == id) it.copy(status = status) else it }
        }
    }

    override suspend fun toggleStatus(id: String) {
        val item = _ingredients.value.find { it.id == id } ?: return
        val newStatus = when (item.status) {
            IngredientStatus.STOCK -> IngredientStatus.OUT_OF_STOCK
            IngredientStatus.OUT_OF_STOCK -> IngredientStatus.CART
            IngredientStatus.CART -> IngredientStatus.STOCK
        }
        updateStatus(id, newStatus)
    }

    override suspend fun deleteIngredient(id: String) {
        val numericId = id.toLongOrNull()
        if (numericId != null) {
            apiService.deleteIngredient(numericId)
        }
        _ingredients.update { list -> list.filterNot { it.id == id } }
    }

    override suspend fun getIngredientById(id: String): Ingredient? {
        return _ingredients.value.find { it.id == id }
    }

    override suspend fun fetchCatalogIngredients(
        query: String?,
        category: IngredientCategory?,
        lang: String
    ): Result<List<com.dahee.blockbyblock.domain.model.CatalogIngredient>> {
        return apiService.getCatalogIngredients(query = query, category = category?.name, lang = lang).map { list ->
            list.map { item ->
                val cat = try {
                    IngredientCategory.valueOf(item.category)
                } catch (_: Exception) {
                    IngredientCategory.OTHER
                }
                com.dahee.blockbyblock.domain.model.CatalogIngredient(
                    id = item.id.toString(),
                    name = item.name,
                    category = cat
                )
            }
        }
    }

    private fun mapResponseToIngredient(res: IngredientResponse): Ingredient {
        val category = try {
            IngredientCategory.valueOf(res.category)
        } catch (_: Exception) {
            IngredientCategory.OTHER
        }

        val status = try {
            IngredientStatus.valueOf(res.status)
        } catch (_: Exception) {
            IngredientStatus.STOCK
        }

        return Ingredient(
            id = res.id.toString(),
            name = res.name,
            category = category,
            status = status,
            updatedAt = getCurrentEpochMillis()
        )
    }
}
