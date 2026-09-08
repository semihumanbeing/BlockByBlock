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

    private data class PageCacheKey(
        val tab: IngredientTab,
        val category: IngredientCategory?,
        val page: Int
    )

    private val pageCache = mutableMapOf<PageCacheKey, com.dahee.blockbyblock.domain.model.IngredientPagedResult>()
    private val activePrefetchJobs = mutableMapOf<PageCacheKey, Job>()
    private var loadJob: Job? = null

    init {
        loadPage(page = 1)
        observeAllIngredients()
    }

    private fun observeAllIngredients() {
        scope.launch {
            repository.getAllIngredients().collectLatest { allList ->
                _uiState.update { current ->
                    current.copy(registeredIngredients = allList)
                }
            }
        }
    }

    fun loadPage(page: Int, forceRefresh: Boolean = false) {
        val tab = _uiState.value.selectedTab
        val category = _uiState.value.selectedCategory
        val key = PageCacheKey(tab, category, page)

        if (!forceRefresh && pageCache.containsKey(key)) {
            val cached = pageCache[key]!!
            applyPagedResult(cached)
            // Background prefetch adjacent pages: next page and previous page
            if (cached.page.hasNext) {
                prefetchPage(tab, category, page + 1)
            }
            if (page > 1) {
                prefetchPage(tab, category, page - 1)
            }
            return
        }

        loadJob?.cancel()
        loadJob = scope.launch {
            _uiState.update { it.copy(isPageLoading = true) }
            val status = tabToStatus(tab)
            val result = repository.fetchIngredientsPaged(
                page = page,
                size = PAGE_SIZE,
                status = status,
                category = category,
                query = null
            )
            result.onSuccess { pagedResult ->
                pageCache[key] = pagedResult
                applyPagedResult(pagedResult)

                // Background prefetch adjacent pages: next page and previous page
                if (pagedResult.page.hasNext) {
                    prefetchPage(tab, category, page + 1)
                }
                if (page > 1) {
                    prefetchPage(tab, category, page - 1)
                }
            }.onFailure {
                _uiState.update { it.copy(isPageLoading = false) }
            }
        }
    }

    private fun prefetchPage(tab: IngredientTab, category: IngredientCategory?, page: Int) {
        if (page < 1) return
        val key = PageCacheKey(tab, category, page)
        if (pageCache.containsKey(key)) return
        if (activePrefetchJobs[key]?.isActive == true) return

        val job = scope.launch {
            try {
                val status = tabToStatus(tab)
                val result = repository.fetchIngredientsPaged(
                    page = page,
                    size = PAGE_SIZE,
                    status = status,
                    category = category,
                    query = null
                )
                result.onSuccess { pagedResult ->
                    pageCache[key] = pagedResult
                }
            } catch (_: Exception) {
                // Silently ignore background prefetch errors
            } finally {
                activePrefetchJobs.remove(key)
            }
        }
        activePrefetchJobs[key] = job
    }

    private fun applyPagedResult(pagedResult: com.dahee.blockbyblock.domain.model.IngredientPagedResult) {
        if (pagedResult.page.items.isEmpty() && pagedResult.page.page > 1) {
            val fallbackPage = pagedResult.page.totalPages.coerceAtLeast(1)
            if (fallbackPage != pagedResult.page.page) {
                loadPage(page = fallbackPage, forceRefresh = true)
                return
            }
        }

        _uiState.update { current ->
            current.copy(
                displayedIngredients = pagedResult.page.items,
                currentPage = pagedResult.page.page,
                pageSize = pagedResult.page.size,
                totalPages = pagedResult.page.totalPages.coerceAtLeast(1),
                totalElements = pagedResult.page.totalElements,
                hasNextPage = pagedResult.page.hasNext,
                hasPreviousPage = pagedResult.page.hasPrevious,
                inStockCount = pagedResult.counts.stock,
                consumedCount = pagedResult.counts.outOfStock,
                shoppingCartCount = pagedResult.counts.cart,
                totalCount = pagedResult.counts.total,
                isPageLoading = false
            )
        }
    }

    private fun tabToStatus(tab: IngredientTab): IngredientStatus? = when (tab) {
        IngredientTab.ALL -> null
        IngredientTab.IN_STOCK -> IngredientStatus.STOCK
        IngredientTab.SHOPPING_CART -> IngredientStatus.CART
    }

    fun onPageChange(targetPage: Int) {
        val maxPage = _uiState.value.totalPages.coerceAtLeast(1)
        val clamped = targetPage.coerceIn(1, maxPage)
        if (clamped == _uiState.value.currentPage) return
        loadPage(page = clamped)
    }

    fun onTabChange(newTab: IngredientTab) {
        if (_uiState.value.selectedTab == newTab) return
        _uiState.update { it.copy(selectedTab = newTab, currentPage = 1) }
        loadPage(page = 1)
    }

    fun onCategoryFilterChange(category: IngredientCategory?) {
        val updated = if (_uiState.value.selectedCategory == category) null else category
        _uiState.update { it.copy(selectedCategory = updated, currentPage = 1) }
        loadPage(page = 1)
    }

    private fun invalidateCacheAndReload() {
        activePrefetchJobs.values.forEach { it.cancel() }
        activePrefetchJobs.clear()
        pageCache.clear()
        loadPage(page = _uiState.value.currentPage, forceRefresh = true)
    }

    // Mark as consumed (Move to consumed state within inventory)
    fun onMarkAsConsumed(id: String) {
        scope.launch {
            repository.updateStatus(id, IngredientStatus.OUT_OF_STOCK)
            invalidateCacheAndReload()
        }
    }

    // Move to Shopping Cart
    fun onMoveToCart(id: String) {
        scope.launch {
            repository.updateStatus(id, IngredientStatus.CART)
            invalidateCacheAndReload()
        }
    }

    // Restore to In Stock
    fun onRestoreToStock(id: String) {
        scope.launch {
            repository.updateStatus(id, IngredientStatus.STOCK)
            invalidateCacheAndReload()
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
                invalidateCacheAndReload()
            }
        }
    }

    // Catalog Search Dialog Handlers
    private var catalogSearchJob: Job? = null

    private var currentLanguage: com.dahee.blockbyblock.core.i18n.AppLanguage = initialLanguage

    fun setLanguage(language: com.dahee.blockbyblock.core.i18n.AppLanguage) {
        if (currentLanguage != language) {
            currentLanguage = language
            if (_uiState.value.isSearchCatalogDialogOpen) {
                searchCatalog(_uiState.value.catalogSearchQuery, _uiState.value.catalogCategoryFilter)
            }
        }
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
                catalogSearchQuery = "",
                catalogCategoryFilter = null,
                catalogTargetStatus = initialStatus,
                catalogResults = initialItems
            )
        }
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

    private fun searchCatalog(query: String, category: IngredientCategory?) {
        catalogSearchJob?.cancel()
        catalogSearchJob = scope.launch {
            if (query.isNotBlank()) {
                delay(150)
            }
            val langCode = currentLanguage.name
            val result = repository.fetchCatalogIngredients(
                query = query.ifBlank { null },
                category = category,
                lang = langCode
            )
            result.onSuccess { items ->
                _uiState.update { current ->
                    if (current.catalogSearchQuery == query && current.catalogCategoryFilter == category) {
                        current.copy(catalogResults = items)
                    } else {
                        current
                    }
                }
            }.onFailure {
                val fallback = MasterIngredientCatalog.search(query, category, langCode)
                _uiState.update { it.copy(catalogResults = fallback) }
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
                    invalidateCacheAndReload()
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
            invalidateCacheAndReload()
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
                    invalidateCacheAndReload()
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
            invalidateCacheAndReload()
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
            invalidateCacheAndReload()
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
            invalidateCacheAndReload()
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
            invalidateCacheAndReload()
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
                invalidateCacheAndReload()
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
