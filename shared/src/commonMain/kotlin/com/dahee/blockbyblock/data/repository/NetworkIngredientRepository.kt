package com.dahee.blockbyblock.data.repository

import com.dahee.blockbyblock.core.utils.getCurrentEpochMillis
import com.dahee.blockbyblock.data.remote.dto.CreateIngredientRequest
import com.dahee.blockbyblock.data.remote.dto.IngredientResponse
import com.dahee.blockbyblock.data.remote.dto.UpdateIngredientRequest
import com.dahee.blockbyblock.data.remote.service.IngredientApiService
import com.dahee.blockbyblock.domain.model.CatalogIngredient
import com.dahee.blockbyblock.domain.model.Ingredient
import com.dahee.blockbyblock.domain.model.IngredientCategory
import com.dahee.blockbyblock.domain.model.IngredientStatus
import com.dahee.blockbyblock.domain.model.PageResult
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
        return apiService.getIngredients(page = 1, size = 1000).map { response ->
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
                _ingredients.update { list ->
                    val idx = list.indexOfFirst { it.id == ingredient.id }
                    if (idx >= 0) {
                        list.map { if (it.id == ingredient.id) updated else it }
                    } else {
                        listOf(updated) + list
                    }
                }
            }.onFailure {
                _ingredients.update { list ->
                    val idx = list.indexOfFirst { it.id == ingredient.id }
                    if (idx >= 0) {
                        list.map { if (it.id == ingredient.id) ingredient else it }
                    } else {
                        listOf(ingredient) + list
                    }
                }
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
        _ingredients.update { list -> list.filterNot { it.id == id } }
        val numericId = id.toLongOrNull()
        if (numericId != null) {
            apiService.deleteIngredient(numericId)
        }
    }

    override suspend fun restoreIngredient(ingredient: Ingredient, insertIndex: Int): Result<Ingredient> {
        // 1. Optimistic restore to local state immediately so UI updates with 0 latency
        insertIngredientAt(ingredient, insertIndex)

        val numericId = ingredient.id.toLongOrNull()
        if (numericId != null) {
            val restoreResult = apiService.restoreIngredient(numericId)
            if (restoreResult.isSuccess) {
                val restored = mapResponseToIngredient(restoreResult.getOrThrow())
                val finalRestored = if (restored.status != ingredient.status) {
                    apiService.updateStatus(numericId, ingredient.status.name)
                    restored.copy(status = ingredient.status)
                } else {
                    restored
                }
                insertIngredientAt(finalRestored, insertIndex)
                return Result.success(finalRestored)
            } else {
                // If restore endpoint fails (e.g. server hard-deleted or 404), recreate via createIngredient
                val createReq = CreateIngredientRequest(
                    name = ingredient.name,
                    status = ingredient.status.name,
                    category = ingredient.category.name
                )
                val createResult = apiService.createIngredient(createReq)
                if (createResult.isSuccess) {
                    val created = mapResponseToIngredient(createResult.getOrThrow())
                    _ingredients.update { list ->
                        list.map { if (it.id == ingredient.id) created else it }
                    }
                    return Result.success(created)
                }
            }
        } else {
            val createReq = CreateIngredientRequest(
                name = ingredient.name,
                status = ingredient.status.name,
                category = ingredient.category.name
            )
            val createResult = apiService.createIngredient(createReq)
            if (createResult.isSuccess) {
                val created = mapResponseToIngredient(createResult.getOrThrow())
                _ingredients.update { list ->
                    list.map { if (it.id == ingredient.id) created else it }
                }
                return Result.success(created)
            }
        }

        return Result.success(ingredient)
    }

    private fun insertIngredientAt(item: Ingredient, insertIndex: Int) {
        _ingredients.update { list ->
            val mutable = list.filterNot { it.id == item.id }.toMutableList()
            if (insertIndex in 0..mutable.size) {
                mutable.add(insertIndex, item)
            } else {
                mutable.add(0, item)
            }
            mutable
        }
    }

    override suspend fun getIngredientById(id: String): Ingredient? {
        return _ingredients.value.find { it.id == id }
    }

    override suspend fun fetchCatalogIngredients(
        query: String?,
        category: IngredientCategory?,
        lang: String
    ): Result<List<CatalogIngredient>> {
        return fetchCatalogIngredientsPaged(query, category, lang, page = 1, size = 100).map { it.items }
    }

    override suspend fun fetchCatalogIngredientsPaged(
        query: String?,
        category: IngredientCategory?,
        lang: String,
        page: Int,
        size: Int
    ): Result<PageResult<CatalogIngredient>> {
        return apiService.getCatalogIngredients(
            query = query,
            category = category?.name,
            lang = lang,
            page = page,
            size = size
        ).map { pageRes ->
            val mappedItems = pageRes.items.map { item ->
                val cat = try {
                    IngredientCategory.valueOf(item.category)
                } catch (_: Exception) {
                    IngredientCategory.OTHER
                }
                CatalogIngredient(
                    id = item.id.toString(),
                    name = item.name,
                    category = cat
                )
            }
            PageResult(
                items = mappedItems,
                page = pageRes.page,
                size = pageRes.size,
                totalElements = pageRes.totalElements,
                totalPages = pageRes.totalPages,
                hasNext = pageRes.hasNext,
                hasPrevious = pageRes.hasPrevious
            )
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
