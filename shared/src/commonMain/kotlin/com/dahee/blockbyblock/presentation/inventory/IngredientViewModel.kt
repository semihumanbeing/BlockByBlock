package com.dahee.blockbyblock.presentation.inventory

import com.dahee.blockbyblock.data.datasource.MasterIngredientCatalog
import com.dahee.blockbyblock.domain.model.CatalogIngredient
import com.dahee.blockbyblock.domain.model.Ingredient
import com.dahee.blockbyblock.domain.model.IngredientCategory
import com.dahee.blockbyblock.domain.model.IngredientStatus
import com.dahee.blockbyblock.domain.model.IngredientUnit
import com.dahee.blockbyblock.domain.repository.IngredientRepository
import com.dahee.blockbyblock.presentation.inventory.state.IngredientTab
import com.dahee.blockbyblock.presentation.inventory.state.IngredientUiState
import com.dahee.blockbyblock.presentation.inventory.state.UndoDeleteState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class IngredientViewModel(
    private val repository: IngredientRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {
    private val _uiState = MutableStateFlow(IngredientUiState())
    val uiState: StateFlow<IngredientUiState> = _uiState.asStateFlow()

    private var toastJob: Job? = null

    init {
        observeIngredients()
    }

    private fun observeIngredients() {
        scope.launch {
            repository.getAllIngredients().collectLatest { allList ->
                val inStock = allList.count { it.status == IngredientStatus.IN_STOCK }
                val consumed = allList.count { it.status == IngredientStatus.CONSUMED }
                val cart = allList.count { it.status == IngredientStatus.SHOPPING_CART }

                val tab = _uiState.value.selectedTab
                val cat = _uiState.value.selectedCategory

                val filtered = allList.filter { item ->
                    val matchesTab = when (tab) {
                        IngredientTab.ALL -> true
                        IngredientTab.IN_STOCK -> item.status == IngredientStatus.IN_STOCK
                        IngredientTab.SHOPPING_CART -> item.status == IngredientStatus.SHOPPING_CART
                    }
                    val matchesCat = cat == null || item.category == cat
                    matchesTab && matchesCat
                }.sortedBy { it.status == IngredientStatus.CONSUMED } // Consumed items appear sorted at bottom in ALL list

                _uiState.update { current ->
                    current.copy(
                        registeredIngredients = allList,
                        allMatchingIngredients = filtered,
                        displayedIngredients = filtered,
                        inStockCount = inStock,
                        consumedCount = consumed,
                        shoppingCartCount = cart,
                        totalCount = allList.size
                    )
                }
            }
        }
    }

    fun onTabChange(newTab: IngredientTab) {
        _uiState.update { it.copy(selectedTab = newTab) }
        refreshFilteredList()
    }

    fun onCategoryFilterChange(category: IngredientCategory?) {
        _uiState.update { current ->
            val updated = if (current.selectedCategory == category) null else category
            current.copy(selectedCategory = updated)
        }
        refreshFilteredList()
    }

    // Mark as consumed (Move to consumed state within inventory)
    fun onMarkAsConsumed(id: String) {
        scope.launch {
            repository.updateStatus(id, IngredientStatus.CONSUMED)
        }
    }

    // Move to Shopping Cart
    fun onMoveToCart(id: String) {
        scope.launch {
            repository.updateStatus(id, IngredientStatus.SHOPPING_CART)
        }
    }

    // Restore to In Stock
    fun onRestoreToStock(id: String) {
        scope.launch {
            repository.updateStatus(id, IngredientStatus.IN_STOCK)
        }
    }

    // Checklist 1-tap 3-state circular toggle: In Stock -> Consumed -> Shopping Cart -> In Stock
    fun onToggleChecklistStatus(id: String) {
        scope.launch {
            val item = _uiState.value.registeredIngredients.find { it.id == id }
            if (item != null) {
                when (item.status) {
                    IngredientStatus.IN_STOCK -> {
                        repository.updateStatus(id, IngredientStatus.CONSUMED)
                    }
                    IngredientStatus.CONSUMED -> {
                        repository.updateStatus(id, IngredientStatus.SHOPPING_CART)
                    }
                    IngredientStatus.SHOPPING_CART -> {
                        repository.updateStatus(id, IngredientStatus.IN_STOCK)
                    }
                }
            }
        }
    }

    // Quick quantity update with immediate auto-save (Kept for domain logic)
    fun onQuickUpdateQuantity(id: String, newQuantity: Double) {
        scope.launch {
            repository.updateQuantityAndUnit(id, newQuantity.coerceAtLeast(0.0))
        }
    }

    // Quick unit update with immediate auto-save (Kept for domain logic)
    fun onQuickUpdateUnit(id: String, newUnit: IngredientUnit) {
        scope.launch {
            repository.updateQuantityAndUnit(
                id,
                _uiState.value.displayedIngredients.find { it.id == id }?.quantity ?: 1.0,
                newUnit.symbol
            )
        }
    }

    // Catalog Search Dialog Handlers
    fun onOpenSearchCatalogDialog() {
        val initialStatus = if (_uiState.value.selectedTab == IngredientTab.SHOPPING_CART) {
            IngredientStatus.SHOPPING_CART
        } else {
            IngredientStatus.IN_STOCK
        }
        _uiState.update {
            it.copy(
                isSearchCatalogDialogOpen = true,
                catalogSearchQuery = "",
                catalogCategoryFilter = null,
                catalogTargetStatus = initialStatus,
                catalogResults = MasterIngredientCatalog.items
            )
        }
    }

    fun onCloseSearchCatalogDialog() {
        _uiState.update { it.copy(isSearchCatalogDialogOpen = false) }
    }

    fun onCatalogSearchQueryChange(query: String) {
        _uiState.update { current ->
            val results = MasterIngredientCatalog.search(query, current.catalogCategoryFilter)
            current.copy(catalogSearchQuery = query, catalogResults = results)
        }
    }

    fun onCatalogCategoryFilterChange(category: IngredientCategory?) {
        _uiState.update { current ->
            val newCat = if (current.catalogCategoryFilter == category) null else category
            val results = MasterIngredientCatalog.search(current.catalogSearchQuery, newCat)
            current.copy(catalogCategoryFilter = newCat, catalogResults = results)
        }
    }

    fun onCatalogTargetStatusChange(status: IngredientStatus) {
        _uiState.update { it.copy(catalogTargetStatus = status) }
    }

    // Add ingredient selected from catalog (re-activates if consumed, prevents duplicates if active)
    fun onAddFromCatalog(
        catalogItem: CatalogIngredient,
        quantity: Double,
        unit: IngredientUnit,
        status: IngredientStatus
    ) {
        val trimmedName = catalogItem.name.trim()
        val existing = _uiState.value.registeredIngredients.find {
            it.name.trim().equals(trimmedName, ignoreCase = true)
        }
        if (existing != null) {
            if (existing.status == IngredientStatus.CONSUMED) {
                // Re-activate consumed ingredient by updating its status to target status
                scope.launch {
                    repository.updateStatus(existing.id, status)
                    val targetText = if (status == IngredientStatus.IN_STOCK) "보유중" else "장바구니"
                    showAutoSaveToast("'${trimmedName}'이(가) ${targetText}에 추가되었습니다.")
                }
                return
            } else {
                val statusText = if (existing.status == IngredientStatus.IN_STOCK) "보유중" else "장바구니"
                showAutoSaveToast("'${trimmedName}'은(는) 이미 ${statusText}에 등록되어 있습니다.")
                return
            }
        }

        scope.launch {
            val newIngredient = Ingredient(
                id = "ing_${Random.nextInt(100000, 999999)}",
                name = trimmedName,
                quantity = quantity,
                unit = unit,
                status = status,
                category = catalogItem.category
            )
            repository.upsertIngredient(newIngredient)
            showAutoSaveToast("'${trimmedName}'이(가) ${if (status == IngredientStatus.IN_STOCK) "보유중" else "장바구니"}에 추가되었습니다.")
        }
    }

    // Add custom ingredient from search text (re-activates if consumed, prevents duplicates if active)
    fun onAddCustomFromCatalogQuery(
        name: String,
        quantity: Double,
        unit: IngredientUnit,
        status: IngredientStatus
    ) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return

        val existing = _uiState.value.registeredIngredients.find {
            it.name.trim().equals(trimmed, ignoreCase = true)
        }
        if (existing != null) {
            if (existing.status == IngredientStatus.CONSUMED) {
                // Re-activate consumed custom ingredient by updating its status to target status
                scope.launch {
                    repository.updateStatus(existing.id, status)
                    val targetText = if (status == IngredientStatus.IN_STOCK) "보유중" else "장바구니"
                    showAutoSaveToast("'${trimmed}'이(가) ${targetText}에 추가되었습니다.")
                }
                return
            } else {
                val statusText = if (existing.status == IngredientStatus.IN_STOCK) "보유중" else "장바구니"
                showAutoSaveToast("'${trimmed}'은(는) 이미 ${statusText}에 등록되어 있습니다.")
                return
            }
        }

        scope.launch {
            val newIngredient = Ingredient(
                id = "ing_${Random.nextInt(100000, 999999)}",
                name = trimmed,
                quantity = quantity,
                unit = unit,
                status = status,
                category = guessCategoryByName(trimmed)
            )
            repository.upsertIngredient(newIngredient)
            showAutoSaveToast("'${trimmed}'이(가) ${if (status == IngredientStatus.IN_STOCK) "보유중" else "장바구니"}에 추가되었습니다.")
        }
    }

    fun onSaveIngredient(ingredient: Ingredient) {
        val trimmed = ingredient.name.trim()
        val existingOther = _uiState.value.registeredIngredients.find {
            it.id != ingredient.id && it.name.trim().equals(trimmed, ignoreCase = true)
        }
        if (existingOther != null) {
            val statusText = when (existingOther.status) {
                IngredientStatus.IN_STOCK -> "보유중"
                IngredientStatus.CONSUMED -> "보유중(소진 상태)"
                IngredientStatus.SHOPPING_CART -> "장바구니"
            }
            showAutoSaveToast("'${trimmed}'은(는) 이미 ${statusText}에 등록되어 있습니다.")
            return
        }

        scope.launch {
            repository.upsertIngredient(ingredient.copy(name = trimmed))
            _uiState.update { it.copy(isAddDialogOpen = false, editingIngredient = null) }
        }
    }

    private var undoJob: Job? = null

    // Delete ingredient with 1-tap Undo support
    fun onDeleteIngredientWithUndo(ingredient: Ingredient, message: String) {
        scope.launch {
            repository.deleteIngredient(ingredient.id)
            undoJob?.cancel()
            _uiState.update {
                it.copy(
                    isAddDialogOpen = false,
                    editingIngredient = null,
                    undoDeleteState = UndoDeleteState(ingredient, message)
                )
            }
            undoJob = scope.launch {
                delay(4000)
                _uiState.update { it.copy(undoDeleteState = null) }
            }
        }
    }

    // Restore last deleted ingredient
    fun onUndoDelete() {
        val lastState = _uiState.value.undoDeleteState ?: return
        undoJob?.cancel()
        scope.launch {
            repository.upsertIngredient(lastState.ingredient)
            _uiState.update { it.copy(undoDeleteState = null) }
        }
    }

    fun onDismissUndo() {
        undoJob?.cancel()
        _uiState.update { it.copy(undoDeleteState = null) }
    }

    fun onDeleteIngredient(id: String) {
        val target = _uiState.value.registeredIngredients.find { it.id == id }
        if (target != null) {
            onDeleteIngredientWithUndo(target, "'${target.name}'이(가) 삭제되었습니다.")
        } else {
            scope.launch {
                repository.deleteIngredient(id)
                _uiState.update { it.copy(isAddDialogOpen = false, editingIngredient = null) }
            }
        }
    }

    fun onOpenEditDialog(ingredient: Ingredient) {
        _uiState.update { it.copy(isAddDialogOpen = false, editingIngredient = ingredient) }
    }

    fun onCloseDialog() {
        _uiState.update { it.copy(isAddDialogOpen = false, editingIngredient = null) }
    }

    private fun showAutoSaveToast(message: String) {
        toastJob?.cancel()
        toastJob = scope.launch {
            _uiState.update { it.copy(autoSaveToast = message) }
            delay(2500)
            _uiState.update { it.copy(autoSaveToast = null) }
        }
    }

    private fun refreshFilteredList() {
        scope.launch {
            repository.getAllIngredients().collectLatest { allList ->
                val tab = _uiState.value.selectedTab
                val cat = _uiState.value.selectedCategory

                val filtered = allList.filter { item ->
                    val matchesTab = when (tab) {
                        IngredientTab.ALL -> true
                        IngredientTab.IN_STOCK -> item.status == IngredientStatus.IN_STOCK
                        IngredientTab.SHOPPING_CART -> item.status == IngredientStatus.SHOPPING_CART
                    }
                    val matchesCat = cat == null || item.category == cat
                    matchesTab && matchesCat
                }.sortedBy { it.status == IngredientStatus.CONSUMED }

                _uiState.update { current ->
                    current.copy(
                        registeredIngredients = allList,
                        allMatchingIngredients = filtered,
                        displayedIngredients = filtered
                    )
                }
            }
        }
    }

    private fun guessCategoryByName(name: String): IngredientCategory {
        val lower = name.lowercase()
        return when {
            listOf("닭", "소고기", "돼지", "연어", "새우", "참치", "고기", "beef", "chicken", "pork", "salmon", "shrimp", "fish").any { lower.contains(it) } ->
                IngredientCategory.MEAT_SEAFOOD
            listOf("양파", "브로콜리", "당근", "파프리카", "마늘", "대파", "버섯", "아보카도", "시금치", "블루베리", "onion", "carrot", "broccoli", "spinach", "vegetable").any { lower.contains(it) } ->
                IngredientCategory.VEGETABLE
            listOf("밥", "오트밀", "파스타", "면", "고구마", "감자", "쌀", "rice", "oat", "pasta", "potato", "bread").any { lower.contains(it) } ->
                IngredientCategory.GRAIN_CARB
            listOf("간장", "올리브유", "기름", "소스", "양념", "후추", "육수", "페스토", "퓨레", "sauce", "oil", "pepper", "broth", "pesto").any { lower.contains(it) } ->
                IngredientCategory.SAUCE_SEASONING
            listOf("계란", "달걀", "요거트", "치즈", "우유", "버터", "egg", "yogurt", "cheese", "milk").any { lower.contains(it) } ->
                IngredientCategory.DAIRY_EGG
            else -> IngredientCategory.OTHER
        }
    }
}
