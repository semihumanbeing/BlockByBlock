package com.dahee.blockbyblock.presentation.inventory.state

import com.dahee.blockbyblock.data.datasource.MasterIngredientCatalog
import com.dahee.blockbyblock.domain.model.CatalogIngredient
import com.dahee.blockbyblock.domain.model.Ingredient
import com.dahee.blockbyblock.domain.model.IngredientCategory
import com.dahee.blockbyblock.domain.model.IngredientStatus

enum class IngredientTab {
    ALL,
    IN_STOCK,
    SHOPPING_CART
}

data class UndoDeleteState(
    val ingredient: Ingredient,
    val message: String
)

data class IngredientUiState(
    val selectedTab: IngredientTab = IngredientTab.IN_STOCK,
    val selectedCategory: IngredientCategory? = null,
    val registeredIngredients: List<Ingredient> = emptyList(),
    val allMatchingIngredients: List<Ingredient> = emptyList(),
    val displayedIngredients: List<Ingredient> = emptyList(),
    val inStockCount: Int = 0,
    val consumedCount: Int = 0,
    val shoppingCartCount: Int = 0,
    val totalCount: Int = 0,
    val isAddDialogOpen: Boolean = false,
    val isSearchCatalogDialogOpen: Boolean = false,
    val catalogSearchQuery: String = "",
    val catalogCategoryFilter: IngredientCategory? = null,
    val catalogTargetStatus: IngredientStatus = IngredientStatus.IN_STOCK,
    val catalogResults: List<CatalogIngredient> = MasterIngredientCatalog.items,
    val editingIngredient: Ingredient? = null,
    val undoDeleteState: UndoDeleteState? = null,
    val autoSaveToast: String? = null
)
