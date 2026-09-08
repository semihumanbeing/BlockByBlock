package com.dahee.blockbyblock.domain.repository

import com.dahee.blockbyblock.domain.model.Ingredient
import com.dahee.blockbyblock.domain.model.IngredientCategory
import com.dahee.blockbyblock.domain.model.IngredientStatus
import kotlinx.coroutines.flow.Flow

interface IngredientRepository {
    fun getAllIngredients(): Flow<List<Ingredient>>
    fun searchIngredients(
        query: String = "",
        statusFilter: IngredientStatus? = null,
        categoryFilter: IngredientCategory? = null
    ): Flow<List<Ingredient>>
    fun getSearchSuggestions(query: String): List<Ingredient>
    suspend fun upsertIngredient(ingredient: Ingredient)
    suspend fun toggleStatus(id: String)
    suspend fun updateStatus(id: String, status: IngredientStatus)
    suspend fun deleteIngredient(id: String)
    suspend fun getIngredientById(id: String): Ingredient?
    suspend fun fetchIngredients(): Result<List<Ingredient>>
    suspend fun fetchIngredientsPaged(
        page: Int = 1,
        size: Int = 12,
        status: IngredientStatus? = null,
        category: IngredientCategory? = null,
        query: String? = null
    ): Result<com.dahee.blockbyblock.domain.model.IngredientPagedResult>
    suspend fun fetchCatalogIngredients(
        query: String? = null,
        category: IngredientCategory? = null,
        lang: String = "KO"
    ): Result<List<com.dahee.blockbyblock.domain.model.CatalogIngredient>> = Result.success(emptyList())
}
