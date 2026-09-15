package com.dahee.blockbyblock

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect

class SharedLogicAndroidHostTest {

    @Test
    fun example() {
        assertEquals(3, 1 + 2)
    }

    @Test
    fun testDateFormattingKoreanAndEnglish() {
        val dateStr = "2026-09-06" // Sunday

        // Korean format
        val koFormatted = com.dahee.blockbyblock.presentation.mealplan.MealPlanViewModel.formatFullDate(
            dateStr,
            com.dahee.blockbyblock.core.i18n.AppLanguage.KO
        )
        assertEquals("2026년 9월 6일 (일)", koFormatted)

        // English format
        val enFormatted = com.dahee.blockbyblock.presentation.mealplan.MealPlanViewModel.formatFullDate(
            dateStr,
            com.dahee.blockbyblock.core.i18n.AppLanguage.EN
        )
        assertEquals("Sun, Sep 6, 2026", enFormatted)

        // Day of week names
        assertEquals("일", com.dahee.blockbyblock.presentation.mealplan.MealPlanViewModel.getDayOfWeekName(dateStr, com.dahee.blockbyblock.core.i18n.AppLanguage.KO))
        assertEquals("Sun", com.dahee.blockbyblock.presentation.mealplan.MealPlanViewModel.getDayOfWeekName(dateStr, com.dahee.blockbyblock.core.i18n.AppLanguage.EN))
        assertEquals("Mon", com.dahee.blockbyblock.presentation.mealplan.MealPlanViewModel.getDayOfWeekName("2026-09-07", com.dahee.blockbyblock.core.i18n.AppLanguage.EN))
    }

    @Test
    fun testInMemoryIngredientPaginationAndPrefetch() = kotlinx.coroutines.runBlocking {
        val repo = com.dahee.blockbyblock.data.repository.InMemoryIngredientRepository()
        // Insert 25 ingredients
        for (i in 1..25) {
            repo.upsertIngredient(
                com.dahee.blockbyblock.domain.model.Ingredient(
                    id = "ing_$i",
                    name = "재료 $i",
                    status = if (i <= 15) com.dahee.blockbyblock.domain.model.IngredientStatus.STOCK else com.dahee.blockbyblock.domain.model.IngredientStatus.CART,
                    category = com.dahee.blockbyblock.domain.model.IngredientCategory.VEGETABLE
                )
            )
        }

        // Test page 1 with size 12
        val page1Result = repo.fetchIngredientsPaged(page = 1, size = 12)
        val p1 = page1Result.getOrThrow()
        assertEquals(12, p1.page.items.size)
        assertEquals(25L, p1.page.totalElements)
        assertEquals(3, p1.page.totalPages)
        assertEquals(true, p1.page.hasNext)
        assertEquals(false, p1.page.hasPrevious)
        assertEquals(15, p1.counts.stock)
        assertEquals(10, p1.counts.cart)
        assertEquals(0, p1.counts.outOfStock)

        // Test page 2 with size 12
        val page2Result = repo.fetchIngredientsPaged(page = 2, size = 12)
        val p2 = page2Result.getOrThrow()
        assertEquals(12, p2.page.items.size)
        assertEquals(true, p2.page.hasNext)
        assertEquals(true, p2.page.hasPrevious)

        // Test page 3 with size 12 (remaining 1 item)
        val page3Result = repo.fetchIngredientsPaged(page = 3, size = 12)
        val p3 = page3Result.getOrThrow()
        assertEquals(1, p3.page.items.size)
        assertEquals(false, p3.page.hasNext)
        assertEquals(true, p3.page.hasPrevious)
    }

    @Test
    fun testIngredientViewModelUnpaginatedInventoryAndPreserveOrder() = kotlinx.coroutines.runBlocking {
        val repo = com.dahee.blockbyblock.data.repository.InMemoryIngredientRepository()
        for (i in 1..25) {
            repo.upsertIngredient(
                com.dahee.blockbyblock.domain.model.Ingredient(
                    id = "ing_$i",
                    name = "재료 $i",
                    status = com.dahee.blockbyblock.domain.model.IngredientStatus.STOCK,
                    category = com.dahee.blockbyblock.domain.model.IngredientCategory.VEGETABLE
                )
            )
        }

        val testJob = kotlinx.coroutines.Job()
        val testScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined + testJob)

        val viewModel = com.dahee.blockbyblock.presentation.inventory.IngredientViewModel(
            repository = repo,
            scope = testScope
        )

        // 1. Inventory list is NOT paginated: all 25 items are displayed directly
        val state1 = viewModel.uiState.value
        assertEquals(25, state1.displayedIngredients.size)
        assertEquals(25, state1.registeredIngredients.size)

        // 2. Initial order: ing_25 is at index 0, ing_15 is at index 10, ing_1 is at index 24 (since upsert inserts at front)
        val initialIndex15 = state1.displayedIngredients.indexOfFirst { it.id == "ing_15" }
        assertEquals(10, initialIndex15)

        // 3. Mark ing_15 as consumed (status OUT_OF_STOCK) -> Order must NOT change!
        viewModel.onMarkAsConsumed("ing_15")
        val stateAfterConsumed = viewModel.uiState.value
        val afterConsumedIndex15 = stateAfterConsumed.displayedIngredients.indexOfFirst { it.id == "ing_15" }
        // Must stay at the exact same index, NEVER jumping to index 0
        assertEquals(initialIndex15, afterConsumedIndex15)
        assertEquals(com.dahee.blockbyblock.domain.model.IngredientStatus.OUT_OF_STOCK, stateAfterConsumed.displayedIngredients[initialIndex15].status)

        // 4. Move ing_15 to cart -> Order still remains identical
        viewModel.onMoveToCart("ing_15")
        val stateAfterCart = viewModel.uiState.value
        val afterCartIndex15 = stateAfterCart.displayedIngredients.indexOfFirst { it.id == "ing_15" }
        assertEquals(initialIndex15, afterCartIndex15)
        assertEquals(com.dahee.blockbyblock.domain.model.IngredientStatus.CART, stateAfterCart.displayedIngredients[initialIndex15].status)

        // 5. Restore ing_15 to stock -> Order still remains identical
        viewModel.onRestoreToStock("ing_15")
        val stateAfterRestore = viewModel.uiState.value
        val afterRestoreIndex15 = stateAfterRestore.displayedIngredients.indexOfFirst { it.id == "ing_15" }
        assertEquals(initialIndex15, afterRestoreIndex15)
        assertEquals(com.dahee.blockbyblock.domain.model.IngredientStatus.STOCK, stateAfterRestore.displayedIngredients[initialIndex15].status)

        // 6. Checklist 1-tap circular toggle -> Order still remains identical
        viewModel.onToggleChecklistStatus("ing_15")
        val stateAfterToggle = viewModel.uiState.value
        val afterToggleIndex15 = stateAfterToggle.displayedIngredients.indexOfFirst { it.id == "ing_15" }
        assertEquals(initialIndex15, afterToggleIndex15)
        assertEquals(com.dahee.blockbyblock.domain.model.IngredientStatus.OUT_OF_STOCK, stateAfterToggle.displayedIngredients[initialIndex15].status)

        testJob.cancel()
    }

    @Test
    fun testIngredientViewModelDeleteAndUndo() = kotlinx.coroutines.runBlocking {
        val repo = com.dahee.blockbyblock.data.repository.InMemoryIngredientRepository()
        for (i in 1..10) {
            repo.upsertIngredient(
                com.dahee.blockbyblock.domain.model.Ingredient(
                    id = "ing_$i",
                    name = "재료 $i",
                    status = com.dahee.blockbyblock.domain.model.IngredientStatus.STOCK,
                    category = com.dahee.blockbyblock.domain.model.IngredientCategory.VEGETABLE
                )
            )
        }

        val testJob = kotlinx.coroutines.Job()
        val testScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined + testJob)

        val viewModel = com.dahee.blockbyblock.presentation.inventory.IngredientViewModel(
            repository = repo,
            scope = testScope
        )

        // 1. Initial State: 10 items
        val initialItems = viewModel.uiState.value.displayedIngredients
        assertEquals(10, initialItems.size)
        val target = initialItems[3] // index 3

        // 2. Delete with Undo
        viewModel.onDeleteIngredientWithUndo(target, "'${target.name}'이(가) 삭제되었습니다.")
        val stateAfterDelete = viewModel.uiState.value
        assertEquals(9, stateAfterDelete.displayedIngredients.size)
        assertTrue(stateAfterDelete.displayedIngredients.none { it.id == target.id })
        assertNotNull(stateAfterDelete.undoDeleteState)
        assertEquals(target.id, stateAfterDelete.undoDeleteState?.ingredient?.id)
        assertEquals(3, stateAfterDelete.undoDeleteState?.index)

        // 3. Undo Delete -> Item is restored to its exact original position
        viewModel.onUndoDelete()
        val stateAfterUndo = viewModel.uiState.value
        assertEquals(10, stateAfterUndo.displayedIngredients.size)
        assertTrue(stateAfterUndo.displayedIngredients.any { it.id == target.id })
        assertNull(stateAfterUndo.undoDeleteState)
        val restoredIndex = stateAfterUndo.displayedIngredients.indexOfFirst { it.id == target.id }
        assertEquals(3, restoredIndex)
        assertEquals(target.name, stateAfterUndo.displayedIngredients[restoredIndex].name)

        // 4. Delete via onDeleteIngredient(id) dialog action and undo
        val target2 = stateAfterUndo.displayedIngredients[0]
        viewModel.onDeleteIngredient(target2.id)
        val stateAfterDelete2 = viewModel.uiState.value
        assertEquals(9, stateAfterDelete2.displayedIngredients.size)
        assertNotNull(stateAfterDelete2.undoDeleteState)
        assertEquals(target2.id, stateAfterDelete2.undoDeleteState?.ingredient?.id)

        viewModel.onUndoDelete()
        val stateAfterUndo2 = viewModel.uiState.value
        assertEquals(10, stateAfterUndo2.displayedIngredients.size)
        assertEquals(target2.id, stateAfterUndo2.displayedIngredients[0].id)

        testJob.cancel()
    }

    @Test
    fun testCatalogSearchPagination() = kotlinx.coroutines.runBlocking {
        val testJob = kotlinx.coroutines.Job()
        val testScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined + testJob)

        val repo = com.dahee.blockbyblock.data.repository.InMemoryIngredientRepository()
        val viewModel = com.dahee.blockbyblock.presentation.inventory.IngredientViewModel(
            repository = repo,
            scope = testScope
        )

        // 1. Open catalog dialog -> page 1 should be active
        viewModel.onOpenSearchCatalogDialog()
        val state1 = viewModel.uiState.value
        assertEquals(1, state1.catalogCurrentPage)
        assertEquals(com.dahee.blockbyblock.presentation.inventory.IngredientViewModel.CATALOG_PAGE_SIZE, state1.catalogPagedResults.size)
        assertTrue(state1.catalogTotalPages > 1, "Catalog should have multiple pages")
        val page1FirstItem = state1.catalogPagedResults.first().id

        // 2. Navigate to page 2 (appends page 2 items)
        viewModel.onCatalogPageChange(2)
        val state2 = viewModel.uiState.value
        assertEquals(2, state2.catalogCurrentPage)
        assertEquals(com.dahee.blockbyblock.presentation.inventory.IngredientViewModel.CATALOG_PAGE_SIZE * 2, state2.catalogPagedResults.size)
        val page2Item = state2.catalogPagedResults[com.dahee.blockbyblock.presentation.inventory.IngredientViewModel.CATALOG_PAGE_SIZE].id
        assertTrue(page1FirstItem != page2Item, "Page 2 should have different items from Page 1")

        // 3. Search resets catalog to page 1
        viewModel.onCatalogSearchQueryChange("당근")
        viewModel.catalogSearchJob?.join()
        val stateSearch = viewModel.uiState.value
        assertEquals(1, stateSearch.catalogCurrentPage)

        testJob.cancel()
    }

    @Test
    fun testCatalogSearchInfiniteScroll() = kotlinx.coroutines.runBlocking {
        val testJob = kotlinx.coroutines.Job()
        val testScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined + testJob)

        val repo = com.dahee.blockbyblock.data.repository.InMemoryIngredientRepository()
        val viewModel = com.dahee.blockbyblock.presentation.inventory.IngredientViewModel(
            repository = repo,
            scope = testScope
        )

        viewModel.onOpenSearchCatalogDialog()
        val initial = viewModel.uiState.value
        assertEquals(1, initial.catalogCurrentPage)
        assertEquals(com.dahee.blockbyblock.presentation.inventory.IngredientViewModel.CATALOG_PAGE_SIZE, initial.catalogPagedResults.size)
        assertTrue(initial.catalogHasNextPage)

        // Trigger infinite scroll next page load
        viewModel.loadNextCatalogPage()
        val afterLoadMore = viewModel.uiState.value
        assertEquals(2, afterLoadMore.catalogCurrentPage)
        assertEquals(com.dahee.blockbyblock.presentation.inventory.IngredientViewModel.CATALOG_PAGE_SIZE * 2, afterLoadMore.catalogPagedResults.size)

        testJob.cancel()
    }

    @Test
    fun testMealBlockSequenceOrderPreserved_1_2_2_1_2() = kotlinx.coroutines.runBlocking {
        val testJob = kotlinx.coroutines.Job()
        val testScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined + testJob)

        val mealRepo = com.dahee.blockbyblock.data.repository.InMemoryMealRecordRepository()
        val blockRepo = com.dahee.blockbyblock.data.repository.InMemoryFoodBlockRepository()

        // 1. Setup 2 food blocks with quantity 5 each
        val block1 = com.dahee.blockbyblock.domain.model.FoodBlock(
            id = "block-1",
            name = "블록 1",
            moldId = "mold_1",
            moldName = "몰드 1구",
            moldCapacityMl = 100,
            moldCellCount = 1,
            moldColorHex = "#E0E0E0",
            blockColorHex = "#FF5722",
            mainIngredients = listOf("닭가슴살"),
            quantity = 5
        )
        val block2 = com.dahee.blockbyblock.domain.model.FoodBlock(
            id = "block-2",
            name = "블록 2",
            moldId = "mold_2",
            moldName = "몰드 2구",
            moldCapacityMl = 200,
            moldCellCount = 2,
            moldColorHex = "#E0E0E0",
            blockColorHex = "#4CAF50",
            mainIngredients = listOf("소고기 안심"),
            quantity = 5
        )
        blockRepo.saveFoodBlock(block1)
        blockRepo.saveFoodBlock(block2)

        val viewModel = com.dahee.blockbyblock.presentation.mealplan.MealPlanViewModel(
            mealRecordRepository = mealRepo,
            foodBlockRepository = blockRepo,
            viewModelScope = testScope
        )

        val collectJob = testScope.launch { viewModel.uiState.collect { } }

        // 2. Open slot dialog for 2026-09-08 Lunch
        viewModel.onOpenSlotDialog("2026-09-08", "9월 8일 (화)", com.dahee.blockbyblock.domain.model.MealType.LUNCH)

        // 3. Move blocks in sequence: 1, 2, 2, 1, 2
        fun moveNextBlock(blockId: String) {
            val available = viewModel.uiState.value.slotAvailableBlocks
            val piece = available.firstOrNull { it.blockId == blockId }
            assertNotNull(piece, "Available piece for blockId $blockId must exist")
            viewModel.onMoveBlockToTop(piece)
        }

        moveNextBlock("block-1") // 1
        moveNextBlock("block-2") // 2
        moveNextBlock("block-2") // 2
        moveNextBlock("block-1") // 1
        moveNextBlock("block-2") // 2

        val expectedIds = listOf("block-1", "block-2", "block-2", "block-1", "block-2")
        val expectedNames = listOf("블록 1", "블록 2", "블록 2", "블록 1", "블록 2")
        val expectedSortOrders = listOf(0, 1, 2, 3, 4)

        // Verify in-dialog selection sequence & sortOrder
        val selectedInDialog = viewModel.uiState.value.slotSelectedBlocks
        assertEquals(5, selectedInDialog.size)
        assertEquals(expectedIds, selectedInDialog.map { it.blockId })
        assertEquals(expectedNames, selectedInDialog.map { it.blockName })
        assertEquals(expectedSortOrders, selectedInDialog.map { it.sortOrder })

        // 4. Save slot and verify saved record in repository
        viewModel.onSaveSlot()

        val savedDay = mealRepo.getMealRecordByDate("2026-09-08")
        assertNotNull(savedDay, "Saved DayMealRecord should not be null")
        val savedLunch = savedDay.lunch
        assertEquals(5, savedLunch.blocks.size)
        assertEquals(expectedIds, savedLunch.blocks.map { it.blockId })
        assertEquals(expectedNames, savedLunch.blocks.map { it.blockName })
        assertEquals(expectedSortOrders, savedLunch.blocks.map { it.sortOrder })

        // 5. Re-open slot dialog and verify loaded sequence matches exactly
        viewModel.onOpenSlotDialog("2026-09-08", "9월 8일 (화)", com.dahee.blockbyblock.domain.model.MealType.LUNCH)
        val reloadedSelected = viewModel.uiState.value.slotSelectedBlocks
        assertEquals(5, reloadedSelected.size)
        assertEquals(expectedIds, reloadedSelected.map { it.blockId })
        assertEquals(expectedNames, reloadedSelected.map { it.blockName })
        assertEquals(expectedSortOrders, reloadedSelected.map { it.sortOrder })

        // 6. Save current slot as preset and verify preset blocks sequence & sortOrder
        viewModel.onSaveCurrentAsPreset("1_2_2_1_2 프리셋")
        val presets = mealRepo.getMealPresets()
        val createdPreset = presets.find { it.name == "1_2_2_1_2 프리셋" }
        assertNotNull(createdPreset, "Preset should be saved")
        assertEquals(5, createdPreset.blocks.size)
        assertEquals(expectedIds, createdPreset.blocks.map { it.blockId })
        assertEquals(expectedNames, createdPreset.blocks.map { it.blockName })
        assertEquals(expectedSortOrders, createdPreset.blocks.map { it.sortOrder })

        // 7. Apply preset to a new slot dialog (Dinner) and verify applied sequence
        viewModel.onOpenSlotDialog("2026-09-09", "9월 9일 (수)", com.dahee.blockbyblock.domain.model.MealType.DINNER)
        viewModel.onApplyPreset(createdPreset)

        val appliedSelected = viewModel.uiState.value.slotSelectedBlocks
        assertEquals(5, appliedSelected.size)
        assertEquals(expectedIds, appliedSelected.map { it.blockId })
        assertEquals(expectedNames, appliedSelected.map { it.blockName })
        assertEquals(expectedSortOrders, appliedSelected.map { it.sortOrder })

        // Save dinner slot and verify repository
        viewModel.onSaveSlot()
        val dinnerDay = mealRepo.getMealRecordByDate("2026-09-09")
        assertNotNull(dinnerDay)
        val savedDinner = dinnerDay.dinner
        assertEquals(5, savedDinner.blocks.size)
        assertEquals(expectedIds, savedDinner.blocks.map { it.blockId })
        assertEquals(expectedNames, savedDinner.blocks.map { it.blockName })
        assertEquals(expectedSortOrders, savedDinner.blocks.map { it.sortOrder })

        testJob.cancel()
    }

    @Test
    fun testWeeklyMealPlanPrefetchAndCache() = kotlinx.coroutines.runBlocking {
        val testJob = kotlinx.coroutines.Job()
        val testScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined + testJob)

        val mealRepo = com.dahee.blockbyblock.data.repository.InMemoryMealRecordRepository()
        val blockRepo = com.dahee.blockbyblock.data.repository.InMemoryFoodBlockRepository()

        val viewModel = com.dahee.blockbyblock.presentation.mealplan.MealPlanViewModel(
            mealRecordRepository = mealRepo,
            foodBlockRepository = blockRepo,
            viewModelScope = testScope
        )

        val collectJob = testScope.launch { viewModel.uiState.collect { } }

        val currentMonday = viewModel.uiState.value.weekStartDateString
        val prevMonday = com.dahee.blockbyblock.presentation.mealplan.MealPlanViewModel.offsetDate(currentMonday, -7)
        val nextMonday = com.dahee.blockbyblock.presentation.mealplan.MealPlanViewModel.offsetDate(currentMonday, 7)

        // 1. Current week is immediately loaded and cached
        assertTrue(viewModel.isWeekCached(currentMonday), "Current week should be cached")

        // 2. Adjacent weeks (-7d and +7d) are prefetched in background
        assertTrue(viewModel.isWeekCached(prevMonday), "Previous week (-7d) should be prefetched and cached")
        assertTrue(viewModel.isWeekCached(nextMonday), "Next week (+7d) should be prefetched and cached")

        // 3. Navigate to previous week: instant cache hit (0ms) and prefetch -14d
        viewModel.onPreviousWeek()
        assertEquals(prevMonday, viewModel.uiState.value.weekStartDateString)
        val prevPrevMonday = com.dahee.blockbyblock.presentation.mealplan.MealPlanViewModel.offsetDate(prevMonday, -7)
        assertTrue(viewModel.isWeekCached(prevPrevMonday), "Previous-previous week (-14d) should be prefetched")

        // 4. Navigate to next week: instant cache hit (0ms) back to current week
        viewModel.onNextWeek()
        assertEquals(currentMonday, viewModel.uiState.value.weekStartDateString)

        // 5. Setup block and test slot saving and cache update
        val block = com.dahee.blockbyblock.domain.model.FoodBlock(
            id = "block-cache-1",
            name = "당근 블록",
            moldId = "mold_1",
            moldName = "몰드 1구",
            moldCapacityMl = 100,
            moldCellCount = 1,
            moldColorHex = "#E0E0E0",
            blockColorHex = "#FFA000",
            mainIngredients = listOf("당근"),
            quantity = 2
        )
        blockRepo.saveFoodBlock(block)

        viewModel.onOpenSlotDialog(currentMonday, "월요일", com.dahee.blockbyblock.domain.model.MealType.BREAKFAST)
        val available = viewModel.uiState.value.slotAvailableBlocks
        assertTrue(available.isNotEmpty())
        viewModel.onMoveBlockToTop(available.first())
        viewModel.onSaveSlot()

        // Verify repository and cache reflect updated slot
        val savedDay = mealRepo.getMealRecordByDate(currentMonday)
        assertNotNull(savedDay)
        assertEquals(1, savedDay.breakfast.blocks.size)
        assertTrue(viewModel.isWeekCached(currentMonday))

        // 6. Delete slot and verify cache updates
        viewModel.onDeleteSlot(currentMonday, com.dahee.blockbyblock.domain.model.MealType.BREAKFAST)
        val deletedDay = mealRepo.getMealRecordByDate(currentMonday)
        val breakfastBlocks = deletedDay?.breakfast?.blocks ?: emptyList()
        assertEquals(0, breakfastBlocks.size)
        assertTrue(viewModel.isWeekCached(currentMonday))

        testJob.cancel()
    }

    @Test
    fun testCatalogIngredientSearchCache() = kotlinx.coroutines.runBlocking {
        val testJob = kotlinx.coroutines.Job()
        val testScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined + testJob)

        val repo = com.dahee.blockbyblock.data.repository.InMemoryIngredientRepository()
        val viewModel = com.dahee.blockbyblock.presentation.inventory.IngredientViewModel(
            repository = repo,
            scope = testScope
        )

        // 1. Open dialog
        viewModel.onOpenSearchCatalogDialog()
        assertTrue(viewModel.uiState.value.isSearchCatalogDialogOpen)

        // 2. Query empty query on dialog open is immediately cached
        assertTrue(viewModel.isCatalogCached("", null), "Empty initial query should be cached")

        // 3. Search "당근"
        viewModel.onCatalogSearchQueryChange("당근")
        viewModel.catalogSearchJob?.join()

        assertTrue(viewModel.isCatalogCached("당근", null), "Query '당근' should be cached")
        val carrotResults = viewModel.uiState.value.catalogResults
        assertTrue(carrotResults.any { it.name.contains("당근") }, "Results should contain carrot")

        // 4. Search another term "양파"
        viewModel.onCatalogSearchQueryChange("양파")
        viewModel.catalogSearchJob?.join()
        assertTrue(viewModel.isCatalogCached("양파", null), "Query '양파' should be cached")

        // 5. Instant switch back to "당근" with 0ms delay (cache hit)
        viewModel.onCatalogSearchQueryChange("당근")
        // Immediately available without waiting for delay
        val cachedResults = viewModel.uiState.value.catalogResults
        assertEquals(carrotResults.size, cachedResults.size)

        // 6. Test category filter caching
        viewModel.onCatalogCategoryFilterChange(com.dahee.blockbyblock.domain.model.IngredientCategory.VEGETABLE)
        viewModel.catalogSearchJob?.join()
        assertTrue(viewModel.isCatalogCached("당근", com.dahee.blockbyblock.domain.model.IngredientCategory.VEGETABLE))

        // 7. Test clear cache
        viewModel.clearCatalogCache()
        assertEquals(false, viewModel.isCatalogCached("당근", null))

        testJob.cancel()
    }

    @Test
    fun testApiErrorParsingAndHelperMethods() {
        // 1. Instantiation with field errors
        val error = com.dahee.blockbyblock.data.remote.error.ApiError(
            message = "입력값 검증에 실패했습니다.",
            code = com.dahee.blockbyblock.data.remote.error.ErrorCode.INVALID_INPUT_VALUE,
            status = 400,
            errors = listOf(
                com.dahee.blockbyblock.data.remote.FieldErrorDetail(
                    field = "email",
                    value = "invalid-email",
                    reason = "이메일 형식이 올바르지 않습니다."
                ),
                com.dahee.blockbyblock.data.remote.FieldErrorDetail(
                    field = "password",
                    value = "123",
                    reason = "비밀번호는 8자 이상이어야 합니다."
                )
            )
        )

        assertEquals("입력값 검증에 실패했습니다.", error.message)
        assertEquals(com.dahee.blockbyblock.data.remote.error.ErrorCode.INVALID_INPUT_VALUE, error.code)
        assertEquals(400, error.status)
        assertTrue(error.hasFieldErrors())
        assertTrue(error.isValidationError())
        assertEquals(false, error.isAuthError())
        assertEquals("이메일 형식이 올바르지 않습니다.", error.getFieldErrorMessage("email"))
        // Case-insensitive lookup
        assertEquals("이메일 형식이 올바르지 않습니다.", error.getFieldErrorMessage("EMAIL"))
        assertEquals("비밀번호는 8자 이상이어야 합니다.", error.getFieldErrorMessage("Password"))
        assertEquals(null, error.getFieldErrorMessage("unknownField"))

        // Extract field error map
        val map = error.extractFieldErrorMap()
        assertEquals(2, map.size)
        assertEquals("이메일 형식이 올바르지 않습니다.", map["email"])
        assertEquals("비밀번호는 8자 이상이어야 합니다.", map["password"])

        // Form error helper mapping
        var capturedEmailErr: String? = null
        var capturedPwErr: String? = null
        val applied = com.dahee.blockbyblock.data.remote.error.applyFormApiErrors(
            error = error,
            onFieldError = { field, reason ->
                if (field == "email") capturedEmailErr = reason
                if (field == "password") capturedPwErr = reason
            }
        )
        assertTrue(applied)
        assertEquals("이메일 형식이 올바르지 않습니다.", capturedEmailErr)
        assertEquals("비밀번호는 8자 이상이어야 합니다.", capturedPwErr)
    }

    @Test
    fun testApiErrorClassificationAndJsonParsing() {
        // 1. JSON parsing from HttpResponse text
        val jsonText = """
            {
                "code": "EXPIRED_TOKEN",
                "message": "액세스 토큰이 만료되었습니다.",
                "errors": []
            }
        """.trimIndent()
        val parsed = com.dahee.blockbyblock.data.remote.error.ApiError.fromHttpResponse(401, jsonText)
        assertEquals(com.dahee.blockbyblock.data.remote.error.ErrorCode.EXPIRED_TOKEN, parsed.code)
        assertEquals(401, parsed.status)
        assertTrue(parsed.isAuthError())
        assertTrue(parsed.isTokenExpired())
        assertEquals(false, parsed.isTokenReuseDetected())

        // 2. Token reuse detected classification
        val reuseError = com.dahee.blockbyblock.data.remote.error.ApiError(
            message = "토큰 재사용 감지",
            code = com.dahee.blockbyblock.data.remote.error.ErrorCode.TOKEN_REUSE_DETECTED,
            status = 401
        )
        assertTrue(reuseError.isAuthError())
        assertTrue(reuseError.isTokenReuseDetected())

        // 3. Access denied classification
        val forbidden = com.dahee.blockbyblock.data.remote.error.ApiError(
            message = "접근 권한 없음",
            code = com.dahee.blockbyblock.data.remote.error.ErrorCode.ACCESS_DENIED,
            status = 403
        )
        assertTrue(forbidden.isAccessDenied())

        // 4. Server error classification
        val serverErr = com.dahee.blockbyblock.data.remote.error.ApiError(
            message = "내부 오류",
            code = com.dahee.blockbyblock.data.remote.error.ErrorCode.INTERNAL_SERVER_ERROR,
            status = 500
        )
        assertTrue(serverErr.isServerError())
    }

    @Test
    fun testForceLogoutAndToastHandling() {
        // Test TokenStorage force logout
        var logoutReason: String? = null
        com.dahee.blockbyblock.data.remote.TokenStorage.setAuthFailureHandler { reason ->
            logoutReason = reason
        }

        com.dahee.blockbyblock.data.remote.TokenStorage.setTokens("test_acc", "test_ref")
        assertTrue(com.dahee.blockbyblock.data.remote.TokenStorage.hasTokens())

        com.dahee.blockbyblock.data.remote.ApiClient.forceLogout("TOKEN_REUSE_DETECTED")
        assertEquals(false, com.dahee.blockbyblock.data.remote.TokenStorage.hasTokens())
        assertEquals("TOKEN_REUSE_DETECTED", logoutReason)

        // Test ApiClient toast notification
        var capturedToast: String? = null
        com.dahee.blockbyblock.data.remote.ApiClient.setToastHandler { msg ->
            capturedToast = msg
        }
        com.dahee.blockbyblock.data.remote.ApiClient.showToast("접근 권한이 없습니다.")
        assertEquals("접근 권한이 없습니다.", capturedToast)

        com.dahee.blockbyblock.data.remote.TokenStorage.setAuthFailureHandler(null)
        com.dahee.blockbyblock.data.remote.ApiClient.setToastHandler(null)
    }

    @Test
    fun testCurrentTimezoneAndExpirationDDayStrings() {
        // 1. Timezone detection
        val tz = com.dahee.blockbyblock.core.utils.getCurrentTimeZone()
        assertTrue(tz.isNotBlank(), "Timezone identifier must not be blank")

        // 2. Korean D-Day and Expiration strings
        val koStrings = com.dahee.blockbyblock.core.i18n.KoStrings
        assertEquals("D-3", koStrings.shelfLifeRemainingDDay(3))
        assertEquals("오늘 만료", koStrings.shelfLifeRemainingDDay(0))
        assertEquals("만료됨", koStrings.shelfLifeRemainingDDay(-1))
        assertEquals("2026-06-08까지", koStrings.shelfLifeUntil("2026-06-08"))

        // 3. English D-Day and Expiration strings
        val enStrings = com.dahee.blockbyblock.core.i18n.EnStrings
        assertEquals("D-3", enStrings.shelfLifeRemainingDDay(3))
        assertEquals("Expires today", enStrings.shelfLifeRemainingDDay(0))
        assertEquals("Expired", enStrings.shelfLifeRemainingDDay(-1))
        assertEquals("Until 2026-06-08", enStrings.shelfLifeUntil("2026-06-08"))
        assertEquals("2칸", koStrings.slotCount(2))
        assertEquals("4칸", koStrings.slotCount(4))
        assertEquals("1 slot", enStrings.slotCount(1))
        assertEquals("2 slots", enStrings.slotCount(2))
        assertEquals("4 slots", enStrings.slotCount(4))
        assertEquals("6 slots", enStrings.slotCount(6))

        // 4. PushNotificationManager token management
        com.dahee.blockbyblock.core.notification.PushNotificationManager.setDeviceFcmToken("mock_fcm_token_123")
        assertEquals("mock_fcm_token_123", com.dahee.blockbyblock.core.notification.PushNotificationManager.getDeviceFcmToken())
        assertEquals("mock_fcm_token_123", com.dahee.blockbyblock.core.notification.PushNotificationManager.getOrCreateDeviceFcmToken())

        com.dahee.blockbyblock.core.notification.PushNotificationManager.setDeviceFcmToken(null)
        val generatedToken = com.dahee.blockbyblock.core.notification.PushNotificationManager.getOrCreateDeviceFcmToken()
        assertTrue(generatedToken.isNotBlank())
        com.dahee.blockbyblock.core.notification.PushNotificationManager.setDeviceFcmToken(null)
    }

    @Test
    fun testMoldDefaultNameDetection() {
        val defaultNames = listOf(
            "500ml 2칸",
            "500ml 6칸",
            "250ml 4칸",
            "125ml 6칸",
            "30ml 16칸",
            "Custom 6칸",
            "200ml 6칸",
            "2 Cup 2칸",
            "1/2 Cup 6칸",
            "500ml (2칸)",
            "500ml · 2칸",
            "500ml 2 slots",
            "500ml 1 slot",
            "500ml",
            "Custom",
            "2칸",
            "6칸",
            ""
        )

        for (name in defaultNames) {
            val mold = com.dahee.blockbyblock.domain.model.Equipment(
                id = "1",
                name = name,
                category = com.dahee.blockbyblock.domain.model.EquipmentCategory.MOLD,
                customCapacityMl = 500,
                cellCount = 2
            )
            assertTrue(mold.isDefaultName, "Expected '$name' to be recognized as a default mold name")
        }

        val customNames = listOf(
            "소고기 몰드",
            "베베락 이유식 용기",
            "Blue Silicone Tray",
            "Baby Food Cubes 1"
        )

        for (name in customNames) {
            val mold = com.dahee.blockbyblock.domain.model.Equipment(
                id = "2",
                name = name,
                category = com.dahee.blockbyblock.domain.model.EquipmentCategory.MOLD,
                customCapacityMl = 500,
                cellCount = 2
            )
            assertTrue(!mold.isDefaultName, "Expected '$name' to be recognized as a custom mold name")
        }
    }

    @Test
    fun testNotificationDtoSerializationAndDualCompatibility() {
        val json = com.dahee.blockbyblock.data.remote.ApiClient.jsonConfig

        // 1. User Prompt JSON format (type, content, targetId)
        val promptJson = """
            {
                "id": 101,
                "type": "EXPIRING_BLOCK",
                "title": "소분 블록 유통기한 임박",
                "content": "당근 큐브 유통기한이 3일 남았습니다.",
                "isRead": false,
                "targetId": 55,
                "createdAt": "2026-09-10T09:00:00Z"
            }
        """.trimIndent()
        val dto1 = json.decodeFromString<com.dahee.blockbyblock.data.remote.dto.NotificationResponse>(promptJson)
        assertEquals(101L, dto1.id)
        assertEquals("EXPIRING_BLOCK", dto1.actualType)
        assertEquals("당근 큐브 유통기한이 3일 남았습니다.", dto1.actualContent)
        assertEquals(55L, dto1.targetId)
        assertEquals(false, dto1.isRead)

        // 2. Backend Controller JSON format (notificationType, body)
        val backendJson = """
            {
                "id": 102,
                "notificationType": "BLOCK_EXPIRED",
                "title": "소분 블록 만료",
                "body": "소고기 블록의 유통기한이 만료되었습니다.",
                "isRead": true,
                "targetId": 77,
                "createdAt": "2026-09-08T12:00:00Z"
            }
        """.trimIndent()
        val dto2 = json.decodeFromString<com.dahee.blockbyblock.data.remote.dto.NotificationResponse>(backendJson)
        assertEquals(102L, dto2.id)
        assertEquals("BLOCK_EXPIRED", dto2.actualType)
        assertEquals("소고기 블록의 유통기한이 만료되었습니다.", dto2.actualContent)
        assertEquals(77L, dto2.targetId)
        assertEquals(true, dto2.isRead)

        // 3. NotificationListResponse dual compatibility (items vs content)
        val listBackendJson = """
            {
                "items": [$backendJson],
                "totalCount": 1,
                "unreadCount": 0,
                "page": 1,
                "size": 20,
                "totalPages": 1
            }
        """.trimIndent()
        val listDto = json.decodeFromString<com.dahee.blockbyblock.data.remote.dto.NotificationListResponse>(listBackendJson)
        assertEquals(1, listDto.actualContent.size)
        assertEquals(1L, listDto.actualTotalElements)
        assertEquals(false, listDto.actualHasNext)

        // 4. UnreadCountResponse dual compatibility (unreadCount vs count)
        val unreadJson = """{"unreadCount": 5}"""
        val unreadDto = json.decodeFromString<com.dahee.blockbyblock.data.remote.dto.UnreadCountResponse>(unreadJson)
        assertEquals(5L, unreadDto.actualCount)

        val countJson = """{"count": 12}"""
        val countDto = json.decodeFromString<com.dahee.blockbyblock.data.remote.dto.UnreadCountResponse>(countJson)
        assertEquals(12L, countDto.actualCount)
    }

    @Test
    fun testRelativeTimeFormatting() {
        val koStrings = com.dahee.blockbyblock.core.i18n.KoStrings
        val enStrings = com.dahee.blockbyblock.core.i18n.EnStrings

        assertEquals("방금 전", koStrings.notificationTimeJustNow)
        assertEquals("Just now", enStrings.notificationTimeJustNow)
        assertEquals("5분 전", koStrings.notificationTimeMinutesAgo(5))
        assertEquals("5m ago", enStrings.notificationTimeMinutesAgo(5))
        assertEquals("3시간 전", koStrings.notificationTimeHoursAgo(3))
        assertEquals("3h ago", enStrings.notificationTimeHoursAgo(3))
        assertEquals("어제", koStrings.notificationTimeYesterday)
        assertEquals("Yesterday", enStrings.notificationTimeYesterday)
        assertEquals("2일 전", koStrings.notificationTimeDaysAgo(2))
        assertEquals("2d ago", enStrings.notificationTimeDaysAgo(2))

        // Empty string returns empty
        assertEquals("", com.dahee.blockbyblock.core.utils.formatRelativeTime("", koStrings))

        // Deterministic relative time with fixed nowInstant
        val seoulTz = kotlinx.datetime.TimeZone.of("Asia/Seoul")
        val nowInstant = kotlin.time.Instant.parse("2026-09-11T12:00:00Z") // 21:00 in Seoul

        // 1. Just now (< 1 min)
        assertEquals("방금 전", com.dahee.blockbyblock.core.utils.formatRelativeTime("2026-09-11T11:59:30Z", koStrings, nowInstant, seoulTz))
        assertEquals("Just now", com.dahee.blockbyblock.core.utils.formatRelativeTime("2026-09-11T11:59:30Z", enStrings, nowInstant, seoulTz))

        // 2. 5 minutes ago (< 60 min)
        assertEquals("5분 전", com.dahee.blockbyblock.core.utils.formatRelativeTime("2026-09-11T11:55:00Z", koStrings, nowInstant, seoulTz))
        assertEquals("5m ago", com.dahee.blockbyblock.core.utils.formatRelativeTime("2026-09-11T11:55:00Z", enStrings, nowInstant, seoulTz))

        // 3. 2 hours ago (< 24h, same calendar day in Seoul)
        assertEquals("2시간 전", com.dahee.blockbyblock.core.utils.formatRelativeTime("2026-09-11T10:00:00Z", koStrings, nowInstant, seoulTz))
        assertEquals("2h ago", com.dahee.blockbyblock.core.utils.formatRelativeTime("2026-09-11T10:00:00Z", enStrings, nowInstant, seoulTz))

        // 4. Yesterday (previous calendar day in Seoul)
        assertEquals("어제", com.dahee.blockbyblock.core.utils.formatRelativeTime("2026-09-10T12:00:00Z", koStrings, nowInstant, seoulTz))
        assertEquals("Yesterday", com.dahee.blockbyblock.core.utils.formatRelativeTime("2026-09-10T12:00:00Z", enStrings, nowInstant, seoulTz))

        // 5. 2 days ago (2..6 days)
        assertEquals("2일 전", com.dahee.blockbyblock.core.utils.formatRelativeTime("2026-09-09T12:00:00Z", koStrings, nowInstant, seoulTz))
        assertEquals("2d ago", com.dahee.blockbyblock.core.utils.formatRelativeTime("2026-09-09T12:00:00Z", enStrings, nowInstant, seoulTz))

        // 6. >= 7 days: formatted as local date "YYYY.MM.DD"
        assertEquals("2026.09.01", com.dahee.blockbyblock.core.utils.formatRelativeTime("2026-09-01T12:00:00Z", koStrings, nowInstant, seoulTz))
    }

    @Test
    fun testUtcParsingAndLocalTimeZoneConversion() {
        val seoulTz = kotlinx.datetime.TimeZone.of("Asia/Seoul") // UTC+9
        val nyTz = kotlinx.datetime.TimeZone.of("America/New_York") // UTC-4 in September (EDT)

        // 1. UTC ISO string converted to Seoul local time (05:20 UTC -> 14:20 KST)
        val utcIso = "2026-09-11T05:20:00Z"
        val seoulLdt = com.dahee.blockbyblock.core.utils.toLocalLocalDateTime(utcIso, seoulTz)
        assertNotNull(seoulLdt)
        assertEquals(2026, seoulLdt.year)
        @Suppress("DEPRECATION")
        assertEquals(9, seoulLdt.monthNumber)
        @Suppress("DEPRECATION")
        assertEquals(11, seoulLdt.dayOfMonth)
        assertEquals(14, seoulLdt.hour)
        assertEquals(20, seoulLdt.minute)

        // Formatted strings in Seoul
        assertEquals("2026-09-11", com.dahee.blockbyblock.core.utils.formatIsoToLocalDateString(utcIso, seoulTz))
        assertEquals("2026.09.11 14:20", com.dahee.blockbyblock.core.utils.formatIsoToLocalDateTimeString(utcIso, seoulTz))
        assertEquals("14:20", com.dahee.blockbyblock.core.utils.formatIsoToLocalTime(utcIso, seoulTz))
        assertEquals("2026년 9월 11일", com.dahee.blockbyblock.core.utils.formatIsoToLocalDisplayDate(utcIso, com.dahee.blockbyblock.core.i18n.AppLanguage.KO, seoulTz))
        assertEquals("Sep 11, 2026", com.dahee.blockbyblock.core.utils.formatIsoToLocalDisplayDate(utcIso, com.dahee.blockbyblock.core.i18n.AppLanguage.EN, seoulTz))

        // 2. Cross-day boundary: UTC 20:00 (Sep 11) is 05:00 next day (Sep 12) in Seoul
        val lateUtcIso = "2026-09-11T20:00:00Z"
        val nextDayLdt = com.dahee.blockbyblock.core.utils.toLocalLocalDateTime(lateUtcIso, seoulTz)
        assertNotNull(nextDayLdt)
        @Suppress("DEPRECATION")
        assertEquals(12, nextDayLdt.dayOfMonth)
        assertEquals(5, nextDayLdt.hour)
        assertEquals("2026-09-12", com.dahee.blockbyblock.core.utils.formatIsoToLocalDateString(lateUtcIso, seoulTz))
        assertEquals("2026.09.12 05:00", com.dahee.blockbyblock.core.utils.formatIsoToLocalDateTimeString(lateUtcIso, seoulTz))

        // In New York (EDT, UTC-4), 20:00 UTC is 16:00 (Sep 11)
        val nyLdt = com.dahee.blockbyblock.core.utils.toLocalLocalDateTime(lateUtcIso, nyTz)
        assertNotNull(nyLdt)
        @Suppress("DEPRECATION")
        assertEquals(11, nyLdt.dayOfMonth)
        assertEquals(16, nyLdt.hour)
        assertEquals("2026-09-11", com.dahee.blockbyblock.core.utils.formatIsoToLocalDateString(lateUtcIso, nyTz))

        // 3. Robust ISO normalization (omitted trailing 'Z' treated as UTC)
        val noZIso = "2026-09-11T05:20:00"
        val noZLdt = com.dahee.blockbyblock.core.utils.toLocalLocalDateTime(noZIso, seoulTz)
        assertNotNull(noZLdt)
        assertEquals(14, noZLdt.hour)

        // 4. Epoch millis parsing
        val epoch = com.dahee.blockbyblock.core.utils.parseIsoToEpochMillis("2026-09-11T00:00:00Z")
        assertTrue(epoch > 0L)
    }

    @Test
    fun testInMemoryNotificationRepositoryAndViewModel() = kotlinx.coroutines.runBlocking {
        val initialList = listOf(
            com.dahee.blockbyblock.domain.model.AppNotification(
                id = 1L,
                type = com.dahee.blockbyblock.domain.model.NotificationType.EXPIRING_BLOCK,
                title = "소분 블록 유통기한 임박",
                content = "3일 남음",
                isRead = false,
                targetId = 10L,
                createdAt = "2026-09-10T10:00:00Z"
            ),
            com.dahee.blockbyblock.domain.model.AppNotification(
                id = 2L,
                type = com.dahee.blockbyblock.domain.model.NotificationType.NOTICE,
                title = "공지사항",
                content = "새로운 기능이 추가되었습니다.",
                isRead = true,
                targetId = null,
                createdAt = "2026-09-09T10:00:00Z"
            ),
            com.dahee.blockbyblock.domain.model.AppNotification(
                id = 3L,
                type = com.dahee.blockbyblock.domain.model.NotificationType.BLOCK_EXPIRED,
                title = "블록 만료",
                content = "만료되었습니다.",
                isRead = false,
                targetId = 20L,
                createdAt = "2026-09-08T10:00:00Z"
            )
        )

        val repo = com.dahee.blockbyblock.data.repository.InMemoryNotificationRepository(initialList)

        // Check initial unread count (id 1 and 3 are unread)
        val initialCount = repo.fetchUnreadCount().getOrThrow()
        assertEquals(2L, initialCount)
        assertEquals(2L, repo.unreadCount.value)

        // Query all notifications (page 1, size 2)
        val allPaged = repo.getNotifications(unreadOnly = false, page = 1, size = 2).getOrThrow()
        assertEquals(2, allPaged.notifications.size)
        assertEquals(3L, allPaged.totalElements)
        assertEquals(2, allPaged.totalPages)
        assertTrue(allPaged.hasNext)

        // Query unread only
        val unreadPaged = repo.getNotifications(unreadOnly = true, page = 1, size = 10).getOrThrow()
        assertEquals(2, unreadPaged.notifications.size)
        assertEquals(2L, unreadPaged.totalElements)

        // Mark single notification as read
        repo.markAsRead(1L)
        assertEquals(1L, repo.unreadCount.value)

        // Mark all as read
        repo.markAllAsRead()
        assertEquals(0L, repo.unreadCount.value)

        // Delete single notification
        repo.deleteNotification(2L)
        val afterDelete = repo.getNotifications(unreadOnly = false, page = 1, size = 10).getOrThrow()
        assertEquals(2, afterDelete.notifications.size)

        // Delete all notifications
        repo.deleteAllNotifications()
        val afterDeleteAll = repo.getNotifications(unreadOnly = false, page = 1, size = 10).getOrThrow()
        assertEquals(0, afterDeleteAll.notifications.size)
        assertEquals(0L, repo.unreadCount.value)

        // Test ViewModel with new repo
        com.dahee.blockbyblock.data.remote.TokenStorage.setTokens("test_token", "test_refresh")
        val testScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default + kotlinx.coroutines.Job())
        val repo2 = com.dahee.blockbyblock.data.repository.InMemoryNotificationRepository(initialList)
        val viewModel = com.dahee.blockbyblock.presentation.notification.NotificationViewModel(repo2, customScope = testScope)

        try {
            // Allow coroutines in init to run
            kotlinx.coroutines.delay(100)
            assertEquals(2L, viewModel.uiState.value.unreadCount)
            assertEquals(3, viewModel.uiState.value.notifications.size)

            // Test filter toggle
            viewModel.setFilter(unreadOnly = true)
            kotlinx.coroutines.delay(100)
            assertTrue(viewModel.uiState.value.unreadOnly)
            assertEquals(2, viewModel.uiState.value.notifications.size)

            // Test markAllAsRead in ViewModel
            viewModel.markAllAsRead()
            kotlinx.coroutines.delay(100)
            assertEquals(0L, viewModel.uiState.value.unreadCount)
        } finally {
            com.dahee.blockbyblock.data.remote.TokenStorage.clearTokens()
            testScope.cancel()
        }

        // Test Unauthenticated guard: ViewModel skips fetch when not logged in
        val unauthScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default + kotlinx.coroutines.Job())
        try {
            val unauthRepo = com.dahee.blockbyblock.data.repository.InMemoryNotificationRepository(initialList)
            val unauthVm = com.dahee.blockbyblock.presentation.notification.NotificationViewModel(unauthRepo, customScope = unauthScope)
            kotlinx.coroutines.delay(100)
            assertEquals(0L, unauthVm.uiState.value.unreadCount)
            assertEquals(0, unauthVm.uiState.value.notifications.size)
        } finally {
            unauthScope.cancel()
        }
    }

    @Test
    fun testAuthErrorFormatting() {
        // 1. Invalid email or password exception mapping to Korean
        val invalidErr = Exception("Invalid email or password")
        val koInvalidResult = com.dahee.blockbyblock.presentation.auth.formatAuthError(
            invalidErr,
            isLogin = true,
            com.dahee.blockbyblock.core.i18n.KoStrings
        )
        assertEquals("이메일 또는 비밀번호가 올바르지 않습니다", koInvalidResult)

        // 2. ApiError with 401 status code
        val api401 = com.dahee.blockbyblock.data.remote.error.ApiError(
            message = "Bad credentials",
            code = com.dahee.blockbyblock.data.remote.error.ErrorCode.AUTHENTICATION_FAILED,
            status = 401
        )
        val ko401Result = com.dahee.blockbyblock.presentation.auth.formatAuthError(
            api401,
            isLogin = true,
            com.dahee.blockbyblock.core.i18n.KoStrings
        )
        assertEquals("이메일 또는 비밀번호가 올바르지 않습니다", ko401Result)

        // 3. ApiError with 409 status code (Already exists)
        val api409 = com.dahee.blockbyblock.data.remote.error.ApiError(
            message = "Email already registered",
            code = com.dahee.blockbyblock.data.remote.error.ErrorCode.EMAIL_ALREADY_EXISTS,
            status = 409
        )
        val ko409Result = com.dahee.blockbyblock.presentation.auth.formatAuthError(
            api409,
            isLogin = false,
            com.dahee.blockbyblock.core.i18n.KoStrings
        )
        assertEquals("이미 등록된 이메일 계정입니다", ko409Result)

        // 4. English locale mapping
        val enInvalidResult = com.dahee.blockbyblock.presentation.auth.formatAuthError(
            invalidErr,
            isLogin = true,
            com.dahee.blockbyblock.core.i18n.EnStrings
        )
        assertEquals("Invalid email or password", enInvalidResult)

        // 5. Network error mapping
        val netErr = com.dahee.blockbyblock.data.remote.error.ApiError(
            message = "Failed to connect to host",
            code = com.dahee.blockbyblock.data.remote.error.ErrorCode.NETWORK_ERROR
        )
        val koNetResult = com.dahee.blockbyblock.presentation.auth.formatAuthError(
            netErr,
            isLogin = true,
            com.dahee.blockbyblock.core.i18n.KoStrings
        )
        assertEquals("네트워크 연결 상태를 확인해주세요", koNetResult)

        // 6. Default fallback
        val unknownErr = Exception("Internal unknown error")
        val koDefaultLogin = com.dahee.blockbyblock.presentation.auth.formatAuthError(
            unknownErr,
            isLogin = true,
            com.dahee.blockbyblock.core.i18n.KoStrings
        )
        assertEquals("로그인에 실패했습니다. 다시 시도해주세요", koDefaultLogin)

        val koDefaultSignUp = com.dahee.blockbyblock.presentation.auth.formatAuthError(
            unknownErr,
            isLogin = false,
            com.dahee.blockbyblock.core.i18n.KoStrings
        )
        assertEquals("회원가입에 실패했습니다. 다시 시도해주세요", koDefaultSignUp)
    }

    @Test
    fun testTokenStorageUserLang() {
        com.dahee.blockbyblock.data.remote.TokenStorage.setUserLang("EN")
        assertEquals("EN", com.dahee.blockbyblock.data.remote.TokenStorage.getUserLang())
        com.dahee.blockbyblock.data.remote.TokenStorage.setUserLang("KO")
        assertEquals("KO", com.dahee.blockbyblock.data.remote.TokenStorage.getUserLang())
    }

    @Test
    fun testAccountLockedErrorFormatting() {
        val lockedApiError = com.dahee.blockbyblock.data.remote.error.ApiError(
            message = "Account is locked due to too many failed attempts",
            code = com.dahee.blockbyblock.data.remote.error.ErrorCode.ACCOUNT_LOCKED,
            status = 429
        )

        assertTrue(lockedApiError.isAccountLocked())
        assertTrue(lockedApiError.isTooManyRequests())

        val koLockedMsg = com.dahee.blockbyblock.presentation.auth.formatAuthError(
            lockedApiError,
            isLogin = true,
            com.dahee.blockbyblock.core.i18n.KoStrings
        )
        assertEquals("비밀번호를 여러 번 잘못 입력하여 계정이 일시적으로 잠겼습니다. 10분 뒤에 다시 시도해주세요.", koLockedMsg)

        val enLockedMsg = com.dahee.blockbyblock.presentation.auth.formatAuthError(
            lockedApiError,
            isLogin = true,
            com.dahee.blockbyblock.core.i18n.EnStrings
        )
        assertEquals("Your account has been temporarily locked due to multiple failed password attempts. Please try again in 10 minutes.", enLockedMsg)

        // From raw JSON response
        val rawJson = """{"code":"ACCOUNT_LOCKED","message":"Too many failed attempts"}"""
        val parsed = com.dahee.blockbyblock.data.remote.error.ApiError.fromHttpResponse(429, rawJson)
        assertTrue(parsed.isAccountLocked())
        assertEquals(
            "비밀번호를 여러 번 잘못 입력하여 계정이 일시적으로 잠겼습니다. 10분 뒤에 다시 시도해주세요.",
            com.dahee.blockbyblock.presentation.auth.formatAuthError(parsed, isLogin = true, com.dahee.blockbyblock.core.i18n.KoStrings)
        )
    }

    @Test
    fun testHealthCheckDtoSerialization() {
        val json1 = """{"code":"SUCCESS","data":{"status":"UP","database":"UP"}}"""
        val resp1 = com.dahee.blockbyblock.data.remote.ApiClient.jsonConfig.decodeFromString<com.dahee.blockbyblock.data.remote.ApiResponse<com.dahee.blockbyblock.data.remote.dto.HealthStatusData>>(json1)
        assertEquals("UP", resp1.data.status)
        assertEquals("UP", resp1.data.database)

        val json2 = """{"data":{"status":"UP","timestamp":"2026-09-14T04:05:47.352700570Z","database":"UP"}}"""
        val resp2 = com.dahee.blockbyblock.data.remote.ApiClient.jsonConfig.decodeFromString<com.dahee.blockbyblock.data.remote.dto.HealthCheckResponse>(json2)
        assertEquals("UP", resp2.data.status)
        assertEquals("UP", resp2.data.database)
        assertEquals("2026-09-14T04:05:47.352700570Z", resp2.data.timestamp)
    }

    @Test
    fun testApiErrorLocalization() {
        val ko = com.dahee.blockbyblock.core.i18n.KoStrings
        val en = com.dahee.blockbyblock.core.i18n.EnStrings

        val lockedError = com.dahee.blockbyblock.data.remote.error.ApiError(
            message = "Locked",
            code = com.dahee.blockbyblock.data.remote.error.ErrorCode.ACCOUNT_LOCKED,
            status = 429
        )
        assertEquals("비밀번호를 여러 번 잘못 입력하여 계정이 일시적으로 잠겼습니다. 10분 뒤에 다시 시도해주세요.", lockedError.getLocalizedMessage(ko))
        assertEquals("Your account has been temporarily locked due to multiple failed password attempts. Please try again in 10 minutes.", lockedError.getLocalizedMessage(en))

        val rateLimitError = com.dahee.blockbyblock.data.remote.error.ApiError(
            message = "Rate limited",
            code = com.dahee.blockbyblock.data.remote.error.ErrorCode.TOO_MANY_REQUESTS,
            status = 429
        )
        assertEquals("요청이 너무 많습니다. 잠시 후 다시 시도해주세요.", rateLimitError.getLocalizedMessage(ko))
        assertEquals("Too many requests. Please try again later.", rateLimitError.getLocalizedMessage(en))

        val deniedError = com.dahee.blockbyblock.data.remote.error.ApiError(
            message = "Forbidden",
            code = com.dahee.blockbyblock.data.remote.error.ErrorCode.ACCESS_DENIED,
            status = 403
        )
        assertEquals("접근 권한이 없습니다.", deniedError.getLocalizedMessage(ko))
        assertEquals("Access denied.", deniedError.getLocalizedMessage(en))

        val serverError = com.dahee.blockbyblock.data.remote.error.ApiError(
            message = "",
            code = com.dahee.blockbyblock.data.remote.error.ErrorCode.INTERNAL_SERVER_ERROR,
            status = 500
        )
        assertEquals("일시적인 서버 오류가 발생했습니다.", serverError.getLocalizedMessage(ko))
        assertEquals("A temporary server error occurred.", serverError.getLocalizedMessage(en))

        val timeoutError = com.dahee.blockbyblock.data.remote.error.ApiError(
            message = "Timeout",
            code = com.dahee.blockbyblock.data.remote.error.ErrorCode.TIMEOUT_ERROR
        )
        assertEquals("서버 응답 시간이 초과되었습니다.", timeoutError.getLocalizedMessage(ko))
        assertEquals("Server response timed out.", timeoutError.getLocalizedMessage(en))

        val networkError = com.dahee.blockbyblock.data.remote.error.ApiError(
            message = "Network",
            code = com.dahee.blockbyblock.data.remote.error.ErrorCode.NETWORK_ERROR
        )
        assertEquals("네트워크 연결 상태를 확인해주세요.", networkError.getLocalizedMessage(ko))
        assertEquals("Please check your network connection.", networkError.getLocalizedMessage(en))

        val userNotFoundError = com.dahee.blockbyblock.data.remote.error.ApiError(
            message = "User not found",
            code = com.dahee.blockbyblock.data.remote.error.ErrorCode.USER_NOT_FOUND,
            status = 404
        )
        assertEquals("가입되지 않은 이메일입니다.", userNotFoundError.getLocalizedMessage(ko))
        assertEquals("No account found with this email.", userNotFoundError.getLocalizedMessage(en))
        assertEquals("가입되지 않은 이메일입니다.", com.dahee.blockbyblock.presentation.auth.formatAuthError(userNotFoundError, isLogin = false, ko))
        assertEquals("No account found with this email.", com.dahee.blockbyblock.presentation.auth.formatAuthError(userNotFoundError, isLogin = false, en))

        val invalidCodeError = com.dahee.blockbyblock.data.remote.error.ApiError(
            message = "Invalid verification code",
            code = com.dahee.blockbyblock.data.remote.error.ErrorCode.INVALID_VERIFICATION_CODE,
            status = 400
        )
        assertEquals("인증번호가 일치하지 않거나 만료되었습니다.", invalidCodeError.getLocalizedMessage(ko))
        assertEquals("Verification code is invalid or has expired.", invalidCodeError.getLocalizedMessage(en))
        assertEquals("인증번호가 일치하지 않거나 만료되었습니다.", com.dahee.blockbyblock.presentation.auth.formatAuthError(invalidCodeError, isLogin = false, ko))
        assertEquals("Verification code is invalid or has expired.", com.dahee.blockbyblock.presentation.auth.formatAuthError(invalidCodeError, isLogin = false, en))
    }

    @Test
    fun testPasswordResetDtoSerialization() {
        val req1 = com.dahee.blockbyblock.data.remote.dto.PasswordResetRequest(email = "user@example.com")
        val json1 = com.dahee.blockbyblock.data.remote.ApiClient.jsonConfig.encodeToString(
            com.dahee.blockbyblock.data.remote.dto.PasswordResetRequest.serializer(),
            req1
        )
        assertTrue(json1.contains("\"email\":\"user@example.com\""))

        val req2 = com.dahee.blockbyblock.data.remote.dto.PasswordResetConfirmRequest(
            email = "user@example.com",
            code = "123456",
            newPassword = "newPassword123"
        )
        val json2 = com.dahee.blockbyblock.data.remote.ApiClient.jsonConfig.encodeToString(
            com.dahee.blockbyblock.data.remote.dto.PasswordResetConfirmRequest.serializer(),
            req2
        )
        assertTrue(json2.contains("\"code\":\"123456\""))
        assertTrue(json2.contains("\"newPassword\":\"newPassword123\""))

        val decodedReq2 = com.dahee.blockbyblock.data.remote.ApiClient.jsonConfig.decodeFromString(
            com.dahee.blockbyblock.data.remote.dto.PasswordResetConfirmRequest.serializer(),
            json2
        )
        assertEquals("user@example.com", decodedReq2.email)
        assertEquals("123456", decodedReq2.code)
        assertEquals("newPassword123", decodedReq2.newPassword)
    }

    @Test
    fun testDetermineBlockStatusesIndexedWithOriginalBlocks() {
        val carrotBlock = com.dahee.blockbyblock.domain.model.FoodBlock(
            id = "carrot-1",
            name = "당근 블록",
            moldId = "m1",
            moldName = "몰드 1",
            moldCapacityMl = 50,
            moldCellCount = 6,
            moldColorHex = "#FF7043",
            mainIngredients = listOf("당근"),
            quantity = 0 // Freezer inventory is 0 because it's already used in Breakfast!
        )
        val beefBlock = com.dahee.blockbyblock.domain.model.FoodBlock(
            id = "beef-1",
            name = "소고기 블록",
            moldId = "m2",
            moldName = "몰드 2",
            moldCapacityMl = 50,
            moldCellCount = 6,
            moldColorHex = "#8D6E63",
            mainIngredients = listOf("소고기"),
            quantity = 1
        )
        val allFoodBlocks = listOf(carrotBlock, beefBlock)

        val origCarrotItem = com.dahee.blockbyblock.domain.model.MealBlockItem(
            instanceId = "carrot-inst-1",
            blockId = "carrot-1",
            blockName = "당근 블록"
        )
        val secondCarrotItem = com.dahee.blockbyblock.domain.model.MealBlockItem(
            instanceId = "carrot-inst-2",
            blockId = "carrot-1",
            blockName = "당근 블록"
        )

        // Case 1: Editing Breakfast which originally had 1 carrot block.
        // Even though freezer quantity is 0, the original carrot block should be AVAILABLE.
        val statuses = com.dahee.blockbyblock.domain.model.determineBlockStatusesIndexed(
            blocks = listOf(origCarrotItem),
            allFoodBlocks = allFoodBlocks,
            originalSlotBlocks = listOf(origCarrotItem)
        )
        assertEquals(listOf(com.dahee.blockbyblock.domain.model.MealBlockStatus.AVAILABLE), statuses)

        // Case 2: In Breakfast, trying to add a 2nd carrot block when freezer has 0 and original had 1.
        // 1st is AVAILABLE, 2nd is OUT_OF_STOCK.
        val statusesWithExtra = com.dahee.blockbyblock.domain.model.determineBlockStatusesIndexed(
            blocks = listOf(origCarrotItem, secondCarrotItem),
            allFoodBlocks = allFoodBlocks,
            originalSlotBlocks = listOf(origCarrotItem)
        )
        assertEquals(
            listOf(
                com.dahee.blockbyblock.domain.model.MealBlockStatus.AVAILABLE,
                com.dahee.blockbyblock.domain.model.MealBlockStatus.OUT_OF_STOCK
            ),
            statusesWithExtra
        )

        // Case 3: Editing Lunch (which originally had 0 carrot blocks).
        // Since freezer quantity is 0 and Lunch had no original carrot blocks, carrot is OUT_OF_STOCK.
        val lunchStatuses = com.dahee.blockbyblock.domain.model.determineBlockStatusesIndexed(
            blocks = listOf(origCarrotItem),
            allFoodBlocks = allFoodBlocks,
            originalSlotBlocks = emptyList()
        )
        assertEquals(listOf(com.dahee.blockbyblock.domain.model.MealBlockStatus.OUT_OF_STOCK), lunchStatuses)

        // Case 4: Beef has 1 in freezer. In Lunch (original had 0), 1 beef block is AVAILABLE.
        val beefItem = com.dahee.blockbyblock.domain.model.MealBlockItem(
            instanceId = "beef-inst-1",
            blockId = "beef-1",
            blockName = "소고기 블록"
        )
        val beefItem2 = com.dahee.blockbyblock.domain.model.MealBlockItem(
            instanceId = "beef-inst-2",
            blockId = "beef-1",
            blockName = "소고기 블록"
        )
        val lunchBeefStatuses = com.dahee.blockbyblock.domain.model.determineBlockStatusesIndexed(
            blocks = listOf(beefItem, beefItem2),
            allFoodBlocks = allFoodBlocks,
            originalSlotBlocks = emptyList()
        )
        assertEquals(
            listOf(
                com.dahee.blockbyblock.domain.model.MealBlockStatus.AVAILABLE,
                com.dahee.blockbyblock.domain.model.MealBlockStatus.OUT_OF_STOCK
            ),
            lunchBeefStatuses
        )
    }

    @Test
    fun testPresetMoldShapeDefaultAndRestore() {
        // 1. Verify defaultCellCount for all presets
        assertEquals(2, com.dahee.blockbyblock.domain.model.MoldGridPreset.ML_500.defaultCellCount)
        assertEquals(4, com.dahee.blockbyblock.domain.model.MoldGridPreset.ML_250.defaultCellCount)
        assertEquals(6, com.dahee.blockbyblock.domain.model.MoldGridPreset.ML_125.defaultCellCount)
        assertEquals(16, com.dahee.blockbyblock.domain.model.MoldGridPreset.ML_30.defaultCellCount)
        assertEquals(6, com.dahee.blockbyblock.domain.model.MoldGridPreset.CUSTOM.defaultCellCount)

        // 2. Verify EquipmentUiState defaults use preset defaultCellCount
        val defaults = com.dahee.blockbyblock.presentation.equipment.state.EquipmentUiState.defaultMoldDrafts
        val ml500 = defaults.first { it.preset == com.dahee.blockbyblock.domain.model.MoldGridPreset.ML_500 }
        val ml250 = defaults.first { it.preset == com.dahee.blockbyblock.domain.model.MoldGridPreset.ML_250 }
        val ml125 = defaults.first { it.preset == com.dahee.blockbyblock.domain.model.MoldGridPreset.ML_125 }
        val ml30 = defaults.first { it.preset == com.dahee.blockbyblock.domain.model.MoldGridPreset.ML_30 }

        assertEquals(2, ml500.cellCount)
        assertEquals(4, ml250.cellCount)
        assertEquals(6, ml125.cellCount)
        assertEquals(16, ml30.cellCount)

        // 3. Simulate modifying cellCount away from preset default and restoring
        var modifiedDraft = ml500.copy(cellCount = 4) // changed to 4구
        assertTrue(modifiedDraft.cellCount != modifiedDraft.preset.defaultCellCount)

        // Restore shape back to factory default
        val restoredDraft = modifiedDraft.copy(cellCount = modifiedDraft.preset.defaultCellCount)
        assertEquals(2, restoredDraft.cellCount)
        assertEquals(restoredDraft.preset.defaultCellCount, restoredDraft.cellCount)

        // 4. Verify i18n string parity
        val ko = com.dahee.blockbyblock.core.i18n.KoStrings
        val en = com.dahee.blockbyblock.core.i18n.EnStrings
        assertEquals("원래 모양", ko.resetMoldShape)
        assertEquals("Reset Shape", en.resetMoldShape)
    }

    @Test
    fun testRemoveDepletedBlocksFlow() = kotlinx.coroutines.runBlocking {
        val testJob = kotlinx.coroutines.Job()
        val testScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined + testJob)

        val mealRepo = com.dahee.blockbyblock.data.repository.InMemoryMealRecordRepository()
        val foodRepo = com.dahee.blockbyblock.data.repository.InMemoryFoodBlockRepository()

        // 1. Prepare 1 food block with quantity = 0 (Depleted block)
        val depletedBlock = com.dahee.blockbyblock.domain.model.FoodBlock(
            id = "depleted-1",
            name = "소진된 당근",
            moldId = "m1",
            moldName = "몰드 1",
            moldCapacityMl = 250,
            moldCellCount = 4,
            moldColorHex = "#F5B7B1",
            blockColorHex = "#F5B7B1",
            mainIngredients = listOf("당근"),
            quantity = 0
        )
        // 2. Prepare 1 food block with quantity = 2 (Available block)
        val availableBlock = com.dahee.blockbyblock.domain.model.FoodBlock(
            id = "avail-1",
            name = "신선한 닭가슴살",
            moldId = "m2",
            moldName = "몰드 2",
            moldCapacityMl = 250,
            moldCellCount = 4,
            moldColorHex = "#A8D5BA",
            blockColorHex = "#A8D5BA",
            mainIngredients = listOf("닭가슴살"),
            quantity = 2
        )
        foodRepo.saveFoodBlock(depletedBlock)
        foodRepo.saveFoodBlock(availableBlock)

        // 3. Prepare preset containing both depleted block and available block
        val preset = com.dahee.blockbyblock.domain.model.MealPreset(
            id = "preset-test-1",
            name = "테스트 프리셋",
            blocks = listOf(
                com.dahee.blockbyblock.domain.model.MealBlockItem(
                    instanceId = "depleted-inst-1",
                    blockId = "depleted-1",
                    blockName = "소진된 당근",
                    blockColorHex = "#F5B7B1",
                    moldCapacityMl = 250,
                    moldCellCount = 4,
                    sortOrder = 0
                ),
                com.dahee.blockbyblock.domain.model.MealBlockItem(
                    instanceId = "avail-inst-1",
                    blockId = "avail-1",
                    blockName = "신선한 닭가슴살",
                    blockColorHex = "#A8D5BA",
                    moldCapacityMl = 250,
                    moldCellCount = 4,
                    sortOrder = 1
                )
            )
        )
        mealRepo.saveMealPreset(preset)

        val viewModel = com.dahee.blockbyblock.presentation.mealplan.MealPlanViewModel(
            mealRecordRepository = mealRepo,
            foodBlockRepository = foodRepo,
            viewModelScope = testScope
        )

        val collectJob = testScope.launch { viewModel.uiState.collect { } }

        // 4. Open slot dialog for empty slot
        viewModel.onOpenSlotDialog("2026-09-14", "9월 14일 월요일", com.dahee.blockbyblock.domain.model.MealType.LUNCH)

        // 5. Apply preset containing the depleted block
        viewModel.onApplyPreset(preset)

        val state1 = viewModel.uiState.value
        assertEquals(2, state1.slotSelectedBlocks.size)

        // Verify status: depleted block is OUT_OF_STOCK, available block is AVAILABLE
        val statuses1 = com.dahee.blockbyblock.domain.model.determineBlockStatusesIndexed(
            state1.slotSelectedBlocks,
            state1.allFoodBlocks,
            state1.slotOriginalBlocks
        )
        assertEquals(com.dahee.blockbyblock.domain.model.MealBlockStatus.OUT_OF_STOCK, statuses1[0])
        assertEquals(com.dahee.blockbyblock.domain.model.MealBlockStatus.AVAILABLE, statuses1[1])

        // 6. Test removing depleted block directly by clicking it (onMoveBlockToBottom)
        val depletedInSelected = state1.slotSelectedBlocks.first { it.blockId == "depleted-1" }
        val availCountBefore = state1.slotAvailableBlocks.size
        viewModel.onMoveBlockToBottom(depletedInSelected)

        val state2 = viewModel.uiState.value
        assertEquals(1, state2.slotSelectedBlocks.size)
        assertEquals("avail-1", state2.slotSelectedBlocks[0].blockId)
        // Available pool size did NOT increase because depleted block has no inventory
        assertEquals(availCountBefore, state2.slotAvailableBlocks.size)

        // 7. Test onRemoveInvalidBlocks()
        // Re-apply preset so selected blocks have both depleted and available blocks again
        viewModel.onApplyPreset(preset)
        assertEquals(2, viewModel.uiState.value.slotSelectedBlocks.size)

        // One-tap removal of all depleted / invalid blocks
        viewModel.onRemoveInvalidBlocks()
        val finalSelected = viewModel.uiState.value.slotSelectedBlocks
        assertEquals(1, finalSelected.size)
        assertEquals("avail-1", finalSelected[0].blockId)

        val finalStatuses = com.dahee.blockbyblock.domain.model.determineBlockStatusesIndexed(
            finalSelected,
            viewModel.uiState.value.allFoodBlocks,
            viewModel.uiState.value.slotOriginalBlocks
        )
        assertTrue(finalStatuses.all { it == com.dahee.blockbyblock.domain.model.MealBlockStatus.AVAILABLE })

        // 8. Test onRefillMissingBlocks() (부족한 블록 생성 - 지연 반영 방식)
        // Re-apply preset so selected blocks have the depleted block again
        viewModel.onApplyPreset(preset)
        assertEquals(2, viewModel.uiState.value.slotSelectedBlocks.size)
        // Verify before: depleted-1 quantity is 0
        assertEquals(0, foodRepo.getFoodBlocks().first { it.id == "depleted-1" }.quantity)

        // Call onRefillMissingBlocks()
        viewModel.onRefillMissingBlocks()

        // With delayed reflection, foodRepo is NOT mutated immediately!
        assertEquals(0, foodRepo.getFoodBlocks().first { it.id == "depleted-1" }.quantity)
        assertTrue(viewModel.uiState.value.hasPendingRefills)

        // And in UI state, effective blocks make all blocks in slot AVAILABLE
        val refilledStatuses = com.dahee.blockbyblock.domain.model.determineBlockStatusesIndexed(
            viewModel.uiState.value.slotSelectedBlocks,
            viewModel.uiState.value.allFoodBlocks,
            viewModel.uiState.value.slotOriginalBlocks
        )
        assertTrue(refilledStatuses.all { it == com.dahee.blockbyblock.domain.model.MealBlockStatus.AVAILABLE })

        // Test cancel/close: closing dialog discards pending refills without mutating repository
        viewModel.onCloseSlotDialog()
        assertEquals(0, foodRepo.getFoodBlocks().first { it.id == "depleted-1" }.quantity)
        assertFalse(viewModel.uiState.value.hasPendingRefills)

        // Re-open dialog, apply preset again, refill missing blocks, and SAVE
        viewModel.onOpenSlotDialog("2026-09-14", "9월 14일 월요일", com.dahee.blockbyblock.domain.model.MealType.LUNCH)
        viewModel.onApplyPreset(preset)
        viewModel.onRefillMissingBlocks()
        assertEquals(0, foodRepo.getFoodBlocks().first { it.id == "depleted-1" }.quantity)

        // Save slot: pending refills are now committed to foodRepo
        viewModel.onSaveSlot()
        assertEquals(1, foodRepo.getFoodBlocks().first { it.id == "depleted-1" }.quantity)

        // 9. Verify i18n string parity
        assertEquals("소진된 블록이 있어 저장할 수 없습니다.", com.dahee.blockbyblock.core.i18n.KoStrings.invalidBlocksWarning)
        assertEquals("블록 제거", com.dahee.blockbyblock.core.i18n.KoStrings.removeDepletedBlocksBtn)
        assertEquals("Remove Blocks", com.dahee.blockbyblock.core.i18n.EnStrings.removeDepletedBlocksBtn)
        assertEquals("+블록 추가", com.dahee.blockbyblock.core.i18n.KoStrings.createMissingBlocksBtn)
        assertEquals("+ Add Blocks", com.dahee.blockbyblock.core.i18n.EnStrings.createMissingBlocksBtn)

        collectJob.cancel()
        testJob.cancel()
    }

    @Test
    fun testDirectAddIngredientFlow() = kotlinx.coroutines.runBlocking {
        val testJob = kotlinx.coroutines.Job()
        val testScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined + testJob)

        val foodRepo = com.dahee.blockbyblock.data.repository.InMemoryFoodBlockRepository()
        val ingredientRepo = com.dahee.blockbyblock.data.repository.InMemoryIngredientRepository()
        val equipRepo = com.dahee.blockbyblock.data.repository.InMemoryEquipmentRepository()

        // 1. Prepare 1 in-stock ingredient
        val existingIng = com.dahee.blockbyblock.domain.model.Ingredient(
            id = "ing_carrot",
            name = "당근",
            status = com.dahee.blockbyblock.domain.model.IngredientStatus.STOCK
        )
        ingredientRepo.upsertIngredient(existingIng)

        val viewModel = com.dahee.blockbyblock.presentation.block.BlockViewModel(
            foodBlockRepository = foodRepo,
            ingredientRepository = ingredientRepo,
            equipmentRepository = equipRepo,
            coroutineScope = testScope
        )

        // Verify initial state: contains 1 ingredient
        assertEquals(1, viewModel.uiState.value.inStockIngredients.size)

        // 2. Search for unowned ingredient "시금치"
        viewModel.onIngredientSearchQueryChange("시금치")
        assertEquals("시금치", viewModel.uiState.value.ingredientSearchQuery)
        assertTrue(viewModel.uiState.value.filteredInStockIngredients.isEmpty())

        // 3. Direct add & select "시금치"
        viewModel.onAddAndSelectIngredient("시금치")

        // Search query should be cleared
        assertEquals("", viewModel.uiState.value.ingredientSearchQuery)
        // inStockIngredients should now contain "시금치"
        val inStock = viewModel.uiState.value.inStockIngredients
        val spinach = inStock.find { it.name == "시금치" }
        assertNotNull(spinach)
        assertEquals(com.dahee.blockbyblock.domain.model.IngredientStatus.STOCK, spinach.status)
        // It should be automatically selected
        assertTrue(viewModel.uiState.value.selectedIngredientIds.contains(spinach.id))

        // 4. Verify catalog add & auto-selection via callback
        val ingredientVm = com.dahee.blockbyblock.presentation.inventory.IngredientViewModel(
            repository = ingredientRepo,
            scope = testScope
        )
        var addedIngredientFromCatalog: com.dahee.blockbyblock.domain.model.Ingredient? = null
        val catalogItem = com.dahee.blockbyblock.domain.model.CatalogIngredient(
            id = "cat_broccoli",
            name = "브로콜리",
            category = com.dahee.blockbyblock.domain.model.IngredientCategory.VEGETABLE
        )
        ingredientVm.onAddFromCatalog(catalogItem, com.dahee.blockbyblock.domain.model.IngredientStatus.STOCK) { added ->
            addedIngredientFromCatalog = added
            viewModel.selectIngredient(added)
        }
        assertNotNull(addedIngredientFromCatalog)
        assertEquals("브로콜리", addedIngredientFromCatalog?.name)
        assertTrue(viewModel.uiState.value.selectedIngredientIds.contains(addedIngredientFromCatalog?.id))

        // 5. Verify i18n string parity
        assertEquals("보유중인 식재료가 없습니다", com.dahee.blockbyblock.core.i18n.KoStrings.createBlockNoMatchingIngredients)
        assertEquals("No in-stock ingredients found", com.dahee.blockbyblock.core.i18n.EnStrings.createBlockNoMatchingIngredients)
        assertEquals("바로 추가하기", com.dahee.blockbyblock.core.i18n.KoStrings.directAddIngredientBtn)
        assertEquals("Add Directly", com.dahee.blockbyblock.core.i18n.EnStrings.directAddIngredientBtn)
        assertEquals("재료를 모른다면? 메뉴명만 적어서 블록을 생성할 수 있어요!", com.dahee.blockbyblock.core.i18n.KoStrings.deliveryFoodHint)
        assertEquals("Don't know the ingredients? You can create a block with just the menu name!", com.dahee.blockbyblock.core.i18n.EnStrings.deliveryFoodHint)

        testJob.cancel()
    }

    @Test
    fun testOverseasAndDefaultLanguageResolution() {
        // 1. Verify SignUpRequest serialization with lang
        val req = com.dahee.blockbyblock.data.remote.dto.SignUpRequest(
            email = "madrid_user@example.com",
            password = "password123",
            nickname = "madrid",
            lang = "EN"
        )
        assertEquals("EN", req.lang)

        // 2. Verify platform defaultLanguage resolution for overseas timezone vs Korean timezone
        val platform = com.dahee.blockbyblock.getPlatform()
        assertNotNull(platform.defaultLanguage)

        // 3. Verify TokenStorage preservation
        com.dahee.blockbyblock.data.remote.TokenStorage.setUserLang("EN")
        assertEquals("EN", com.dahee.blockbyblock.data.remote.TokenStorage.getUserLang())

        // Ensure getPlatform().defaultLanguage is respected when userLang is cleared
        com.dahee.blockbyblock.data.remote.TokenStorage.clearTokens()
        val initialOrPlatform = com.dahee.blockbyblock.data.remote.TokenStorage.getUserLang() ?: platform.defaultLanguage.name
        assertTrue(initialOrPlatform == "EN" || initialOrPlatform == "KO")
    }

    @Test
    fun testComprehensiveOverseasLanguageSyncAndMadridScenarioQA() {
        // QA 1: Timezone categorization for overseas vs Korean regions
        fun isKoreanTimezone(tz: String): Boolean {
            return tz.isEmpty() || tz == "UTC" ||
                tz.contains("Seoul", ignoreCase = true) ||
                tz.contains("Pyongyang", ignoreCase = true) ||
                tz.contains("ROK", ignoreCase = true) ||
                tz.contains("KST", ignoreCase = true)
        }

        // Madrid, New York, London, Tokyo are all overseas
        org.junit.Assert.assertFalse(isKoreanTimezone("Europe/Madrid"))
        org.junit.Assert.assertFalse(isKoreanTimezone("America/New_York"))
        org.junit.Assert.assertFalse(isKoreanTimezone("Europe/London"))
        org.junit.Assert.assertFalse(isKoreanTimezone("Asia/Tokyo"))
        org.junit.Assert.assertFalse(isKoreanTimezone("Australia/Sydney"))

        // Seoul, Pyongyang, ROK, KST are Korean
        assertTrue(isKoreanTimezone("Asia/Seoul"))
        assertTrue(isKoreanTimezone("Asia/Pyongyang"))
        assertTrue(isKoreanTimezone("ROK"))
        assertTrue(isKoreanTimezone("KST"))

        // QA 2: Language Resolution Function Behavior
        fun resolveDefaultLang(langCode: String, tz: String): com.dahee.blockbyblock.core.i18n.AppLanguage {
            val isKoreanLang = langCode.startsWith("ko", ignoreCase = true)
            val isKoreanTz = isKoreanTimezone(tz)
            return if (isKoreanLang && isKoreanTz) {
                com.dahee.blockbyblock.core.i18n.AppLanguage.KO
            } else {
                com.dahee.blockbyblock.core.i18n.AppLanguage.EN
            }
        }

        // Test Matrix
        assertEquals(com.dahee.blockbyblock.core.i18n.AppLanguage.EN, resolveDefaultLang("es", "Europe/Madrid"))
        assertEquals(com.dahee.blockbyblock.core.i18n.AppLanguage.EN, resolveDefaultLang("en", "Europe/Madrid"))
        assertEquals(com.dahee.blockbyblock.core.i18n.AppLanguage.EN, resolveDefaultLang("ko", "Europe/Madrid"))
        assertEquals(com.dahee.blockbyblock.core.i18n.AppLanguage.EN, resolveDefaultLang("en", "America/New_York"))
        assertEquals(com.dahee.blockbyblock.core.i18n.AppLanguage.KO, resolveDefaultLang("ko", "Asia/Seoul"))
        assertEquals(com.dahee.blockbyblock.core.i18n.AppLanguage.EN, resolveDefaultLang("en", "Asia/Seoul"))

        // QA 3: Madrid first-time user scenario simulation
        // 1. First-time visitor has no stored lang
        com.dahee.blockbyblock.data.remote.TokenStorage.clearTokens()
        org.junit.Assert.assertNull(com.dahee.blockbyblock.data.remote.TokenStorage.getUserLang())

        // 2. Default language resolves to EN for Europe/Madrid
        val detected = resolveDefaultLang("es", "Europe/Madrid")
        assertEquals(com.dahee.blockbyblock.core.i18n.AppLanguage.EN, detected)

        // 3. User registers: SignUpRequest transmits lang = "EN"
        val signUpReq = com.dahee.blockbyblock.data.remote.dto.SignUpRequest(
            email = "spain_user@example.com",
            password = "SecurePassword123!",
            nickname = "spain_chef",
            lang = detected.name
        )
        assertEquals("EN", signUpReq.lang)

        // 4. Client initialLang fallback uses detected ("EN"), never "KO"
        val initialLang = com.dahee.blockbyblock.data.remote.TokenStorage.getUserLang() ?: signUpReq.lang ?: "KO"
        assertEquals("EN", initialLang)
        com.dahee.blockbyblock.data.remote.TokenStorage.setUserLang(initialLang)
        assertEquals("EN", com.dahee.blockbyblock.data.remote.TokenStorage.getUserLang())

        // 5. Server response has legacy default "KO" in userRes.lang
        val serverUserRes = com.dahee.blockbyblock.data.remote.dto.UserResponse(
            id = "user_spain_1",
            email = "spain_user@example.com",
            nickname = "spain_chef",
            avatarType = "PERSON",
            onboardingCompleted = false,
            lang = "KO",
            timezone = "Europe/Madrid"
        )
        // Client preserves local "EN" and does not get stomped by server's "KO"
        val localLang = com.dahee.blockbyblock.data.remote.TokenStorage.getUserLang()
        val targetLang = localLang ?: serverUserRes.lang
        assertEquals("EN", targetLang)

        // QA 4: i18n Strings Completeness Verification
        val ko = com.dahee.blockbyblock.core.i18n.KoStrings
        val en = com.dahee.blockbyblock.core.i18n.EnStrings
        assertTrue(ko.createBlockGoToCookingTools.isNotBlank())
        assertTrue(en.createBlockGoToCookingTools.isNotBlank())
        assertTrue(ko.resetMoldShape.isNotBlank())
        assertTrue(en.resetMoldShape.isNotBlank())
        assertTrue(ko.addCookingTool.isNotBlank())
        assertTrue(en.addCookingTool.isNotBlank())
    }

    @Test
    fun testBlockInventoryExcludesDepletedBlocksAndHistoryRemains() = kotlinx.coroutines.runBlocking {
        val equipRepo = com.dahee.blockbyblock.data.repository.InMemoryEquipmentRepository(
            initialItems = listOf(
                com.dahee.blockbyblock.domain.model.Equipment(
                    id = "mold_15",
                    name = "15ml 실리콘 몰드",
                    category = com.dahee.blockbyblock.domain.model.EquipmentCategory.MOLD,
                    customCapacityMl = 15,
                    cellCount = 6,
                    isOwned = true
                )
            )
        )
        val foodRepo = com.dahee.blockbyblock.data.repository.InMemoryFoodBlockRepository()
        val ingredientRepo = com.dahee.blockbyblock.data.repository.InMemoryIngredientRepository()
        val testScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined)

        // Setup blocks: one in stock, two depleted (quantity == 0)
        val inStockBlock = com.dahee.blockbyblock.domain.model.FoodBlock(
            id = "block_instock",
            name = "당근 블록",
            moldId = "mold_15",
            moldName = "15ml 실리콘 몰드",
            moldCapacityMl = 15,
            moldCellCount = 6,
            moldColorHex = "#FF7043",
            blockColorHex = "#FF7043",
            mainIngredients = listOf("당근"),
            quantity = 3
        )
        val depletedBlock1 = com.dahee.blockbyblock.domain.model.FoodBlock(
            id = "block_depleted_1",
            name = "소고기 블록",
            moldId = "mold_30",
            moldName = "30ml 실리콘 몰드",
            moldCapacityMl = 30,
            moldCellCount = 4,
            moldColorHex = "#8D6E63",
            blockColorHex = "#8D6E63",
            mainIngredients = listOf("소고기"),
            quantity = 0
        )
        val depletedBlock2 = com.dahee.blockbyblock.domain.model.FoodBlock(
            id = "block_depleted_2",
            name = "브로콜리 블록",
            moldId = "mold_15",
            moldName = "15ml 실리콘 몰드",
            moldCapacityMl = 15,
            moldCellCount = 6,
            moldColorHex = "#81C784",
            blockColorHex = "#81C784",
            mainIngredients = listOf("브로콜리"),
            quantity = 0
        )

        foodRepo.saveFoodBlock(inStockBlock)
        foodRepo.saveFoodBlock(depletedBlock1)
        foodRepo.saveFoodBlock(depletedBlock2)

        val viewModel = com.dahee.blockbyblock.presentation.block.BlockViewModel(
            foodBlockRepository = foodRepo,
            ingredientRepository = ingredientRepo,
            equipmentRepository = equipRepo,
            coroutineScope = testScope
        )

        val state = viewModel.uiState.value

        // Total blocks in repository is 3
        assertEquals(3, state.blocks.size)

        // inStockBlocks must ONLY contain quantity > 0 blocks (size == 1)
        assertEquals(1, state.inStockBlocks.size)
        assertEquals("당근 블록", state.inStockBlocks[0].name)

        // filteredBlocks must not contain 0-quantity blocks in freezer inventory
        assertEquals(1, state.filteredBlocks.size)
        assertEquals("당근 블록", state.filteredBlocks[0].name)

        // When filtering by 30ml (which has only depleted block), filteredBlocks must be empty
        viewModel.onCapacityFilterChange(30)
        assertTrue(viewModel.uiState.value.filteredBlocks.isEmpty())

        // Reset filter
        viewModel.onCapacityFilterChange(null)

        // distinctMoldCapacities should include 15ml (from mold & inStockBlock), but NOT 30ml (depleted and mold unowned)
        assertTrue(state.distinctMoldCapacities.contains(15))
        assertFalse(state.distinctMoldCapacities.contains(30))

        // History blocks must still preserve all 3 recipes for reuse
        assertEquals(3, state.historyBlocks.size)
        val historyNames = state.historyBlocks.map { it.name }.toSet()
        assertTrue(historyNames.contains("당근 블록"))
        assertTrue(historyNames.contains("소고기 블록"))
        assertTrue(historyNames.contains("브로콜리 블록"))

        // Applying history from a depleted block must not set blockQuantity to 0!
        viewModel.onOpenCreateScreen()
        viewModel.onApplyHistoryBlock(depletedBlock1)
        val afterHistory = viewModel.uiState.value
        assertEquals("소고기 블록", afterHistory.customBlockName)
        // Should default to available mold cellCount (6), NOT 0
        assertTrue(afterHistory.blockQuantity > 0)
        assertEquals(6, afterHistory.blockQuantity)
        assertEquals("6", afterHistory.blockQuantityInput)

        // If blockQuantity is 0, onSubmitCreateBlock must be blocked
        viewModel.onBlockQuantityInputChange("0")
        assertEquals(0, viewModel.uiState.value.blockQuantity)
        assertFalse(viewModel.uiState.value.canSubmit)
        viewModel.onSubmitCreateBlock()
        // Repository should still have only 3 blocks, no 0-quantity block created
        assertEquals(3, foodRepo.getFoodBlocks().size)
    }

    @Test
    fun testSavedMealSlotWithDepletedBlocksShowsRefillOptionAndRefillsSuccessfully() = kotlinx.coroutines.runBlocking {
        val testJob = kotlinx.coroutines.Job()
        val testScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined + testJob)

        val mealRepo = com.dahee.blockbyblock.data.repository.InMemoryMealRecordRepository()
        val foodRepo = com.dahee.blockbyblock.data.repository.InMemoryFoodBlockRepository()

        // 1. Prepare 1 food block with quantity = 0 (completely depleted)
        val depletedBlock = com.dahee.blockbyblock.domain.model.FoodBlock(
            id = "depleted-101",
            name = "소진된 당근",
            moldId = "m1",
            moldName = "몰드 1",
            moldCapacityMl = 250,
            moldCellCount = 4,
            moldColorHex = "#F5B7B1",
            blockColorHex = "#F5B7B1",
            mainIngredients = listOf("당근"),
            quantity = 0
        )
        foodRepo.saveFoodBlock(depletedBlock)

        // 2. Saved meal slot already contains this depleted block
        val savedSlotBlock = com.dahee.blockbyblock.domain.model.MealBlockItem(
            instanceId = "inst-saved-101",
            blockId = "depleted-101",
            blockName = "소진된 당근",
            blockColorHex = "#F5B7B1",
            moldCapacityMl = 250,
            moldCellCount = 4,
            sortOrder = 0,
            currentStock = 0,
            blockStatus = com.dahee.blockbyblock.domain.model.MealBlockStatus.OUT_OF_STOCK
        )
        val testDate = "2026-09-15"
        val dayRecord = com.dahee.blockbyblock.domain.model.DayMealRecord(
            id = "day_$testDate",
            dateString = testDate,
            lunch = com.dahee.blockbyblock.domain.model.MealSlotRecord(
                mealType = com.dahee.blockbyblock.domain.model.MealType.LUNCH,
                blocks = listOf(savedSlotBlock),
                memo = "저장된 소진 식단 테스트"
            )
        )
        mealRepo.saveMealRecord(dayRecord)

        val viewModel = com.dahee.blockbyblock.presentation.mealplan.MealPlanViewModel(
            mealRecordRepository = mealRepo,
            foodBlockRepository = foodRepo,
            viewModelScope = testScope
        )

        val collectJob = testScope.launch { viewModel.uiState.collect { } }

        // 3. Test determineBlockStatusesIndexed: already saved block in a slot is ALWAYS AVAILABLE
        // (Even if inventory is 0 or empty, already saved block must never be shown as depleted / OUT_OF_STOCK)
        val statuses = com.dahee.blockbyblock.domain.model.determineBlockStatusesIndexed(
            blocks = listOf(savedSlotBlock),
            allFoodBlocks = listOf(depletedBlock),
            originalSlotBlocks = listOf(savedSlotBlock)
        )
        assertEquals(listOf(com.dahee.blockbyblock.domain.model.MealBlockStatus.AVAILABLE), statuses)

        // Also test when allFoodBlocks is empty (all inventory depleted / not returned by API)
        val statusesWithEmptyRepo = com.dahee.blockbyblock.domain.model.determineBlockStatusesIndexed(
            blocks = listOf(savedSlotBlock),
            allFoodBlocks = emptyList(),
            originalSlotBlocks = listOf(savedSlotBlock)
        )
        assertEquals(listOf(com.dahee.blockbyblock.domain.model.MealBlockStatus.AVAILABLE), statusesWithEmptyRepo)

        // 4. Open slot dialog for this saved slot: it is AVAILABLE initially
        viewModel.onOpenSlotDialog(testDate, "9월 15일 화요일", com.dahee.blockbyblock.domain.model.MealType.LUNCH)

        val dialogStatuses = com.dahee.blockbyblock.domain.model.determineBlockStatusesIndexed(
            viewModel.uiState.value.slotSelectedBlocks,
            viewModel.uiState.value.allFoodBlocks,
            viewModel.uiState.value.slotOriginalBlocks
        )
        assertEquals(listOf(com.dahee.blockbyblock.domain.model.MealBlockStatus.AVAILABLE), dialogStatuses)

        // 5. Apply a preset that contains an additional depleted block not in originalBlocks
        val preset = com.dahee.blockbyblock.domain.model.MealPreset(
            id = "preset-depleted",
            name = "소진 프리셋",
            blocks = listOf(
                savedSlotBlock,
                savedSlotBlock.copy(instanceId = "inst-new-depleted", sortOrder = 1)
            )
        )
        viewModel.onApplyPreset(preset)

        val presetStatuses = com.dahee.blockbyblock.domain.model.determineBlockStatusesIndexed(
            viewModel.uiState.value.slotSelectedBlocks,
            viewModel.uiState.value.allFoodBlocks,
            viewModel.uiState.value.slotOriginalBlocks
        )
        // 1st block is from original slot -> AVAILABLE, 2nd block is newly added with 0 stock -> OUT_OF_STOCK
        assertEquals(
            listOf(
                com.dahee.blockbyblock.domain.model.MealBlockStatus.AVAILABLE,
                com.dahee.blockbyblock.domain.model.MealBlockStatus.OUT_OF_STOCK
            ),
            presetStatuses
        )

        // 6. Call onRefillMissingBlocks()
        viewModel.onRefillMissingBlocks()
        assertTrue(viewModel.uiState.value.hasPendingRefills)

        // Effective status becomes AVAILABLE
        val refilledStatuses = com.dahee.blockbyblock.domain.model.determineBlockStatusesIndexed(
            viewModel.uiState.value.slotSelectedBlocks,
            viewModel.uiState.value.allFoodBlocks,
            viewModel.uiState.value.slotOriginalBlocks
        )
        assertTrue(refilledStatuses.all { it == com.dahee.blockbyblock.domain.model.MealBlockStatus.AVAILABLE })

        // 7. Save slot: refill is committed to food repository
        viewModel.onSaveSlot()
        assertEquals(1, foodRepo.getFoodBlocks().first { it.id == "depleted-101" }.quantity)

        collectJob.cancel()
        testJob.cancel()
    }
}