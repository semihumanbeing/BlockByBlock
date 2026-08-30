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
    suspend fun updateQuantityAndUnit(id: String, quantity: Double, unitSymbol: String? = null)
    suspend fun deleteIngredient(id: String)
    suspend fun getIngredientById(id: String): Ingredient?
}
