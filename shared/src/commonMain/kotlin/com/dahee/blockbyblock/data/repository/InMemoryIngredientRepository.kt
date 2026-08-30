package com.dahee.blockbyblock.data.repository

import com.dahee.blockbyblock.domain.model.Ingredient
import com.dahee.blockbyblock.domain.model.IngredientCategory
import com.dahee.blockbyblock.domain.model.IngredientStatus
import com.dahee.blockbyblock.domain.model.IngredientUnit
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
                        ingredient.category.displayNameEn.contains(query, ignoreCase = true) ||
                        "${ingredient.quantity.toInt()}${ingredient.unit.symbol}".contains(query, ignoreCase = true)

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
                IngredientStatus.IN_STOCK -> IngredientStatus.CONSUMED
                IngredientStatus.CONSUMED -> IngredientStatus.SHOPPING_CART
                IngredientStatus.SHOPPING_CART -> IngredientStatus.IN_STOCK
            }
            currentList[index] = item.copy(status = newStatus)
            _ingredients.value = currentList
        }
    }

    override suspend fun updateQuantityAndUnit(id: String, quantity: Double, unitSymbol: String?) {
        val currentList = _ingredients.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index >= 0) {
            val item = currentList[index]
            val newUnit = if (unitSymbol != null) {
                IngredientUnit.entries.find { it.symbol.equals(unitSymbol, ignoreCase = true) } ?: item.unit
            } else {
                item.unit
            }
            currentList[index] = item.copy(quantity = quantity, unit = newUnit)
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
}
