package com.dahee.blockbyblock

import kotlin.test.Test
import kotlin.test.assertEquals

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
    fun testIngredientViewModelPagingAndCache() = kotlinx.coroutines.runBlocking {
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

        // Initial state should load page 1
        val state1 = viewModel.uiState.value
        assertEquals(1, state1.currentPage)
        assertEquals(12, state1.displayedIngredients.size)
        assertEquals(3, state1.totalPages)
        assertEquals(true, state1.hasNextPage)
        assertEquals(false, state1.hasPreviousPage)

        // Switch to page 2
        viewModel.onPageChange(2)
        val state2 = viewModel.uiState.value
        assertEquals(2, state2.currentPage)
        assertEquals(12, state2.displayedIngredients.size)
        assertEquals(true, state2.hasNextPage)
        assertEquals(true, state2.hasPreviousPage)

        // Switch to page 3
        viewModel.onPageChange(3)
        val state3 = viewModel.uiState.value
        assertEquals(3, state3.currentPage)
        assertEquals(1, state3.displayedIngredients.size)
        assertEquals(false, state3.hasNextPage)
        assertEquals(true, state3.hasPreviousPage)

        // Switch back to page 2 (instant from cache)
        viewModel.onPageChange(2)
        val stateBack2 = viewModel.uiState.value
        assertEquals(2, stateBack2.currentPage)
        assertEquals(12, stateBack2.displayedIngredients.size)

        testJob.cancel()
    }
}