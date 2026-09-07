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
}