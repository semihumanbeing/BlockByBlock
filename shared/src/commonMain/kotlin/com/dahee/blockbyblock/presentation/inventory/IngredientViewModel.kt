package com.dahee.blockbyblock.presentation.inventory

import com.dahee.blockbyblock.data.datasource.MasterIngredientCatalog
import com.dahee.blockbyblock.domain.model.CatalogIngredient
import com.dahee.blockbyblock.domain.model.Ingredient
import com.dahee.blockbyblock.domain.model.IngredientCategory
import com.dahee.blockbyblock.domain.model.IngredientStatus
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
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main),
    initialLanguage: com.dahee.blockbyblock.core.i18n.AppLanguage = com.dahee.blockbyblock.core.i18n.AppLanguage.KO
) {
    private val _uiState = MutableStateFlow(IngredientUiState())
    val uiState: StateFlow<IngredientUiState> = _uiState.asStateFlow()

    private var toastJob: Job? = null
    private var loadJob: Job? = null

    // In-memory cache for master catalog search queries keyed by query, category, and language
    private data class CatalogCacheKey(
        val query: String,
        val category: IngredientCategory?,
        val lang: String
    )

    private val catalogCache = mutableMapOf<CatalogCacheKey, List<CatalogIngredient>>()

    init {
        observeAllIngredients()
        loadInitialIngredients()
    }

    private fun observeAllIngredients() {
        scope.launch {
            repository.getAllIngredients().collectLatest { allList ->
                _uiState.update { current ->
                    val filtered = filterIngredients(allList, current.selectedTab, current.selectedCategory)
                    current.copy(
                        registeredIngredients = allList,
                        displayedIngredients = filtered,
                        inStockCount = allList.count { it.status == IngredientStatus.STOCK },
                        consumedCount = allList.count { it.status == IngredientStatus.OUT_OF_STOCK },
                        shoppingCartCount = allList.count { it.status == IngredientStatus.CART },
                        totalCount = allList.size,
                        currentPage = 1,
                        pageSize = allList.size.coerceAtLeast(1),
                        totalPages = 1,
                        totalElements = allList.size.toLong(),
                        hasNextPage = false,
                        hasPreviousPage = false,
                        isPageLoading = false
                    )
                }
            }
        }
    }

    fun loadInitialIngredients(forceRefresh: Boolean = false) {
        loadJob?.cancel()
        loadJob = scope.launch {
            _uiState.update { it.copy(isPageLoading = true) }
            repository.fetchIngredients()
            _uiState.update { it.copy(isPageLoading = false) }
        }
    }

    fun loadPage(page: Int = 1, forceRefresh: Boolean = false) {
        loadInitialIngredients(forceRefresh)
    }

    private fun filterIngredients(
        items: List<Ingredient>,
        tab: IngredientTab,
        category: IngredientCategory?
    ): List<Ingredient> {
        return items.filter { item ->
            val matchesTab = when (tab) {
                IngredientTab.ALL -> true
                IngredientTab.IN_STOCK -> item.status == IngredientStatus.STOCK
                IngredientTab.SHOPPING_CART -> item.status == IngredientStatus.CART
            }
            val matchesCategory = category == null || item.category == category
            matchesTab && matchesCategory
        }
    }

    fun onPageChange(targetPage: Int) {
        // Unpaginated inventory: no-op preserved for interface compatibility
    }

    fun onTabChange(newTab: IngredientTab) {
        if (_uiState.value.selectedTab == newTab) return
        _uiState.update { current ->
            val filtered = filterIngredients(current.registeredIngredients, newTab, current.selectedCategory)
            current.copy(selectedTab = newTab, displayedIngredients = filtered)
        }
    }

    fun onCategoryFilterChange(category: IngredientCategory?) {
        val updated = if (_uiState.value.selectedCategory == category) null else category
        _uiState.update { current ->
            val filtered = filterIngredients(current.registeredIngredients, current.selectedTab, updated)
            current.copy(selectedCategory = updated, displayedIngredients = filtered)
        }
    }

    // Mark as consumed (Move to consumed state within inventory)
    fun onMarkAsConsumed(id: String) {
        scope.launch {
            repository.updateStatus(id, IngredientStatus.OUT_OF_STOCK)
        }
    }

    // Move to Shopping Cart
    fun onMoveToCart(id: String) {
        scope.launch {
            repository.updateStatus(id, IngredientStatus.CART)
        }
    }

    // Restore to In Stock
    fun onRestoreToStock(id: String) {
        scope.launch {
            repository.updateStatus(id, IngredientStatus.STOCK)
        }
    }

    // Checklist 1-tap 3-state circular toggle: In Stock -> Consumed -> Shopping Cart -> In Stock
    fun onToggleChecklistStatus(id: String) {
        scope.launch {
            val item = _uiState.value.displayedIngredients.find { it.id == id }
                ?: _uiState.value.registeredIngredients.find { it.id == id }
            if (item != null) {
                val nextStatus = when (item.status) {
                    IngredientStatus.STOCK -> IngredientStatus.OUT_OF_STOCK
                    IngredientStatus.OUT_OF_STOCK -> IngredientStatus.CART
                    IngredientStatus.CART -> IngredientStatus.STOCK
                }
                repository.updateStatus(id, nextStatus)
            }
        }
    }

    // Catalog Search Dialog Handlers
    internal var catalogSearchJob: Job? = null

    private var currentLanguage: com.dahee.blockbyblock.core.i18n.AppLanguage = initialLanguage

    fun setLanguage(language: com.dahee.blockbyblock.core.i18n.AppLanguage) {
        if (currentLanguage != language) {
            currentLanguage = language
            if (_uiState.value.isSearchCatalogDialogOpen) {
                searchCatalog(_uiState.value.catalogSearchQuery, _uiState.value.catalogCategoryFilter)
            }
        }
    }

    private fun updateCatalogResultsState(
        allResults: List<CatalogIngredient>,
        targetPage: Int = 1,
        query: String = _uiState.value.catalogSearchQuery,
        category: IngredientCategory? = _uiState.value.catalogCategoryFilter
    ) {
        val totalPages = if (allResults.isEmpty()) 1 else ((allResults.size - 1) / CATALOG_PAGE_SIZE) + 1
        val safePage = targetPage.coerceIn(1, totalPages)
        val startIndex = (safePage - 1) * CATALOG_PAGE_SIZE
        val pagedItems = allResults.drop(startIndex).take(CATALOG_PAGE_SIZE)

        _uiState.update { current ->
            current.copy(
                catalogSearchQuery = query,
                catalogCategoryFilter = category,
                catalogResults = allResults,
                catalogPagedResults = pagedItems,
                catalogCurrentPage = safePage,
                catalogPageSize = CATALOG_PAGE_SIZE,
                catalogTotalPages = totalPages
            )
        }
    }

    fun onCatalogPageChange(targetPage: Int) {
        val totalPages = _uiState.value.catalogTotalPages.coerceAtLeast(1)
        val clamped = targetPage.coerceIn(1, totalPages)
        if (clamped == _uiState.value.catalogCurrentPage) return
        updateCatalogResultsState(
            allResults = _uiState.value.catalogResults,
            targetPage = clamped
        )
    }

    fun onOpenSearchCatalogDialog() {
        val initialStatus = if (_uiState.value.selectedTab == IngredientTab.SHOPPING_CART) {
            IngredientStatus.CART
        } else {
            IngredientStatus.STOCK
        }
        val initialItems = if (currentLanguage == com.dahee.blockbyblock.core.i18n.AppLanguage.EN) {
            MasterIngredientCatalog.englishItems
        } else {
            MasterIngredientCatalog.items
        }
        _uiState.update {
            it.copy(
                isSearchCatalogDialogOpen = true,
                catalogTargetStatus = initialStatus
            )
        }
        val cacheKey = CatalogCacheKey(query = "", category = null, lang = currentLanguage.name)
        catalogCache[cacheKey] = initialItems
        updateCatalogResultsState(allResults = initialItems, targetPage = 1, query = "", category = null)
        searchCatalog("", null)
    }

    fun onCloseSearchCatalogDialog() {
        catalogSearchJob?.cancel()
        _uiState.update { it.copy(isSearchCatalogDialogOpen = false) }
    }

    fun onCatalogSearchQueryChange(query: String) {
        _uiState.update { it.copy(catalogSearchQuery = query) }
        searchCatalog(query, _uiState.value.catalogCategoryFilter)
    }

    fun onCatalogCategoryFilterChange(category: IngredientCategory?) {
        val newCat = if (_uiState.value.catalogCategoryFilter == category) null else category
        _uiState.update { it.copy(catalogCategoryFilter = newCat) }
        searchCatalog(_uiState.value.catalogSearchQuery, newCat)
    }

    fun isCatalogCached(query: String, category: IngredientCategory?, lang: String = currentLanguage.name): Boolean {
        return catalogCache.containsKey(CatalogCacheKey(query = query.trim(), category = category, lang = lang))
    }

    fun clearCatalogCache() {
        catalogCache.clear()
    }

    private fun searchCatalog(query: String, category: IngredientCategory?) {
        val trimmedQuery = query.trim()
        val langCode = currentLanguage.name
        val cacheKey = CatalogCacheKey(
            query = trimmedQuery,
            category = category,
            lang = langCode
        )

        // If cached in memory, display immediately with 0ms delay
        val cached = catalogCache[cacheKey]
        if (cached != null) {
            catalogSearchJob?.cancel()
            if (_uiState.value.catalogSearchQuery == query && _uiState.value.catalogCategoryFilter == category) {
                updateCatalogResultsState(allResults = cached, targetPage = 1, query = query, category = category)
            }
            return
        }

        catalogSearchJob?.cancel()
        catalogSearchJob = scope.launch {
            if (trimmedQuery.isNotBlank()) {
                delay(300) // 300ms debounce to prevent burst requests while typing
            }
            val result = repository.fetchCatalogIngredients(
                query = trimmedQuery.ifBlank { null },
                category = category,
                lang = langCode
            )
            result.onSuccess { items ->
                catalogCache[cacheKey] = items
                if (_uiState.value.catalogSearchQuery == query && _uiState.value.catalogCategoryFilter == category) {
                    updateCatalogResultsState(allResults = items, targetPage = 1, query = query, category = category)
                }
            }.onFailure {
                val fallback = MasterIngredientCatalog.search(trimmedQuery, category, langCode)
                catalogCache[cacheKey] = fallback
                if (_uiState.value.catalogSearchQuery == query && _uiState.value.catalogCategoryFilter == category) {
                    updateCatalogResultsState(allResults = fallback, targetPage = 1, query = query, category = category)
                }
            }
        }
    }

    fun onCatalogTargetStatusChange(status: IngredientStatus) {
        _uiState.update { it.copy(catalogTargetStatus = status) }
    }

    // Add ingredient selected from catalog (re-activates if consumed, prevents duplicates if active)
    fun onAddFromCatalog(
        catalogItem: CatalogIngredient,
        status: IngredientStatus
    ) {
        val trimmedName = catalogItem.name.trim()
        val existing = _uiState.value.registeredIngredients.find {
            it.name.trim().equals(trimmedName, ignoreCase = true)
        }
        if (existing != null) {
            if (existing.status == IngredientStatus.OUT_OF_STOCK) {
                // Re-activate consumed ingredient by updating its status to target status
                scope.launch {
                    repository.updateStatus(existing.id, status)
                    val targetText = if (status == IngredientStatus.STOCK) "보유중" else "장바구니"
                    showAutoSaveToast("'${trimmedName}'이(가) ${targetText}에 추가되었습니다.")
                }
                return
            } else {
                val statusText = if (existing.status == IngredientStatus.STOCK) "보유중" else "장바구니"
                showAutoSaveToast("'${trimmedName}'은(는) 이미 ${statusText}에 등록되어 있습니다.")
                return
            }
        }

        scope.launch {
            val newIngredient = Ingredient(
                id = "ing_${Random.nextInt(100000, 999999)}",
                name = trimmedName,
                status = status,
                category = catalogItem.category
            )
            repository.upsertIngredient(newIngredient)
            showAutoSaveToast("'${trimmedName}'이(가) ${if (status == IngredientStatus.STOCK) "보유중" else "장바구니"}에 추가되었습니다.")
        }
    }

    // Add custom ingredient from search text (re-activates if consumed, prevents duplicates if active)
    fun onAddCustomFromCatalogQuery(
        name: String,
        status: IngredientStatus
    ) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return

        val existing = _uiState.value.registeredIngredients.find {
            it.name.trim().equals(trimmed, ignoreCase = true)
        }
        if (existing != null) {
            if (existing.status == IngredientStatus.OUT_OF_STOCK) {
                // Re-activate consumed custom ingredient by updating its status to target status
                scope.launch {
                    repository.updateStatus(existing.id, status)
                    val targetText = if (status == IngredientStatus.STOCK) "보유중" else "장바구니"
                    showAutoSaveToast("'${trimmed}'이(가) ${targetText}에 추가되었습니다.")
                }
                return
            } else {
                val statusText = if (existing.status == IngredientStatus.STOCK) "보유중" else "장바구니"
                showAutoSaveToast("'${trimmed}'은(는) 이미 ${statusText}에 등록되어 있습니다.")
                return
            }
        }

        scope.launch {
            val newIngredient = Ingredient(
                id = "ing_${Random.nextInt(100000, 999999)}",
                name = trimmed,
                status = status,
                category = guessCategoryByName(trimmed)
            )
            repository.upsertIngredient(newIngredient)
            showAutoSaveToast("'${trimmed}'이(가) ${if (status == IngredientStatus.STOCK) "보유중" else "장바구니"}에 추가되었습니다.")
        }
    }

    fun onSaveIngredient(ingredient: Ingredient) {
        val trimmed = ingredient.name.trim()
        val existingOther = _uiState.value.registeredIngredients.find {
            it.id != ingredient.id && it.name.trim().equals(trimmed, ignoreCase = true)
        }
        if (existingOther != null) {
            val statusText = when (existingOther.status) {
                IngredientStatus.STOCK -> "보유중"
                IngredientStatus.OUT_OF_STOCK -> "보유중(소진 상태)"
                IngredientStatus.CART -> "장바구니"
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
        val target = _uiState.value.displayedIngredients.find { it.id == id }
            ?: _uiState.value.registeredIngredients.find { it.id == id }
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

    companion object {
        const val CATALOG_PAGE_SIZE = 8
        private const val PAGE_SIZE = 12
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
