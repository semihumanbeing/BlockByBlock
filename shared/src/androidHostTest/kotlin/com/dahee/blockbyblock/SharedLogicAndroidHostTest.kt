package com.dahee.blockbyblock

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.launch
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

        // 2. Navigate to page 2
        viewModel.onCatalogPageChange(2)
        val state2 = viewModel.uiState.value
        assertEquals(2, state2.catalogCurrentPage)
        assertEquals(com.dahee.blockbyblock.presentation.inventory.IngredientViewModel.CATALOG_PAGE_SIZE, state2.catalogPagedResults.size)
        val page2FirstItem = state2.catalogPagedResults.first().id
        assertTrue(page1FirstItem != page2FirstItem, "Page 2 should have different items from Page 1")

        // 3. Search resets catalog to page 1
        viewModel.onCatalogSearchQueryChange("당근")
        viewModel.catalogSearchJob?.join()
        val stateSearch = viewModel.uiState.value
        assertEquals(1, stateSearch.catalogCurrentPage)

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
}