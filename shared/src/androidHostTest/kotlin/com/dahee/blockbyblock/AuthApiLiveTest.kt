package com.dahee.blockbyblock

import com.dahee.blockbyblock.data.remote.ApiClient
import com.dahee.blockbyblock.data.remote.TokenStorage
import com.dahee.blockbyblock.data.remote.dto.LoginRequest
import com.dahee.blockbyblock.data.remote.dto.UpdateProfileRequest
import com.dahee.blockbyblock.data.remote.service.AuthApiService
import com.dahee.blockbyblock.data.remote.service.UserApiService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthApiLiveTest {

    private val authService = AuthApiService()
    private val userService = UserApiService()

    @Before
    fun setup() {
        ApiClient.baseUrl = "http://localhost:8000"
    }

    @Test
    fun testLoginAndProfileFlow() = runBlocking {
        // 1. Signup/Login with a fresh test user
        val testEmail = "test_${System.currentTimeMillis()}@test.com"
        val signupResult = authService.signUp(com.dahee.blockbyblock.data.remote.dto.SignUpRequest(
            email = testEmail,
            password = "password123",
            nickname = "Tester"
        ))
        assertTrue("Signup failed: ${signupResult.exceptionOrNull()?.message}", signupResult.isSuccess)
        val loginData = signupResult.getOrThrow()
        assertNotNull(loginData.accessToken)
        assertTrue(TokenStorage.isAuthenticated)
        assertNotNull(TokenStorage.getUserId())

        // 2. Fetch profile via /users/me
        val meResult = userService.getMe()
        assertTrue("getMe failed: ${meResult.exceptionOrNull()?.message}", meResult.isSuccess)
        val me = meResult.getOrThrow()
        assertEquals(testEmail, me.email)
        assertNotNull(me.lang)

        // 3. Update profile
        val updateResult = userService.updateProfile(UpdateProfileRequest(nickname = "테스터_수정", avatarType = "CHEF", lang = "EN"))
        assertTrue("updateProfile failed: ${updateResult.exceptionOrNull()?.message}", updateResult.isSuccess)
        assertEquals("테스터_수정", updateResult.getOrThrow().nickname)
        assertEquals("EN", updateResult.getOrThrow().lang)
        assertEquals("EN", TokenStorage.getUserLang())

        // 4. Test Equipment Sync and Fetch
        val equipRepo = com.dahee.blockbyblock.data.repository.NetworkEquipmentRepository()
        val testEquipments = listOf(
            com.dahee.blockbyblock.domain.model.Equipment(
                id = "mold_1",
                name = "500ml 1칸",
                category = com.dahee.blockbyblock.domain.model.EquipmentCategory.MOLD,
                moldPreset = com.dahee.blockbyblock.domain.model.MoldGridPreset.ML_500,
                customCapacityMl = 500,
                cellCount = 1,
                quantity = 2,
                moldColorHex = "#BAE6FD"
            ),
            com.dahee.blockbyblock.domain.model.Equipment(
                id = "tool_MICROWAVE",
                name = "전자레인지",
                category = com.dahee.blockbyblock.domain.model.EquipmentCategory.COOKING_TOOL,
                toolType = com.dahee.blockbyblock.domain.model.CookingToolType.MICROWAVE,
                isOwned = true
            )
        )
        val syncResult = equipRepo.syncAll(testEquipments)
        assertTrue("Equipments sync failed: ${syncResult.exceptionOrNull()?.message}", syncResult.isSuccess)
        val fetchedList = syncResult.getOrThrow()
        assertTrue("Should contain at least 1 mold", fetchedList.any { it.category == com.dahee.blockbyblock.domain.model.EquipmentCategory.MOLD })

        // 5. Test Ingredient Create, Status Update, and Delete
        val ingredientRepo = com.dahee.blockbyblock.data.repository.NetworkIngredientRepository()
        ingredientRepo.upsertIngredient(
            com.dahee.blockbyblock.domain.model.Ingredient(
                id = "",
                name = "소고기 안심",
                category = com.dahee.blockbyblock.domain.model.IngredientCategory.MEAT_SEAFOOD,
                status = com.dahee.blockbyblock.domain.model.IngredientStatus.STOCK
            )
        )
        val ingredientsResult = ingredientRepo.fetchIngredients()
        assertTrue("Ingredients fetch failed", ingredientsResult.isSuccess)
        val createdIngredient = ingredientsResult.getOrThrow().find { it.name == "소고기 안심" }
        assertNotNull("Created ingredient should exist", createdIngredient)

        // Update status to CART
        val ingredientId = createdIngredient!!.id
        ingredientRepo.updateStatus(ingredientId, com.dahee.blockbyblock.domain.model.IngredientStatus.CART)

        // Test Catalog Ingredients Search (KO and EN)
        val catalogResultKo = ingredientRepo.fetchCatalogIngredients(query = "소고기", lang = "KO")
        assertTrue("Catalog KO ingredients fetch failed", catalogResultKo.isSuccess)
        val catalogListKo = catalogResultKo.getOrThrow()
        assertTrue("Catalog should contain KO search results", catalogListKo.isNotEmpty())
        assertTrue("Catalog KO should contain 소고기", catalogListKo.any { it.name.contains("소고기") })

        val catalogResultEn = ingredientRepo.fetchCatalogIngredients(query = "Beef", lang = "EN")
        assertTrue("Catalog EN ingredients fetch failed", catalogResultEn.isSuccess)
        val catalogListEn = catalogResultEn.getOrThrow()
        assertTrue("Catalog should contain EN search results", catalogListEn.isNotEmpty())
        assertTrue("Catalog EN should contain Beef", catalogListEn.any { it.name.contains("Beef") })

        // Delete ingredient
        ingredientRepo.deleteIngredient(ingredientId)

        // 6. Test Food Block Create, Fetch, and Delete
        val blockRepo = com.dahee.blockbyblock.data.repository.NetworkFoodBlockRepository()
        val allEquips = equipRepo.getEquipments().first()
        val mold = allEquips.first { it.category == com.dahee.blockbyblock.domain.model.EquipmentCategory.MOLD }
        val testBlock = com.dahee.blockbyblock.domain.model.FoodBlock(
            id = "",
            name = "소고기 볶음 블록",
            moldId = mold.id,
            moldName = mold.name,
            moldCapacityMl = mold.displayCapacity,
            moldCellCount = mold.cellCount,
            moldColorHex = mold.moldColorHex,
            blockColorHex = "#FF7043",
            mainIngredients = listOf("소고기 안심"),
            subIngredients = listOf("양파", "당근"),
            quantity = 2,
            shelfLifeDays = 30,
            cookingInstructions = listOf(
                com.dahee.blockbyblock.domain.model.CookingInstruction(
                    toolType = com.dahee.blockbyblock.domain.model.CookingToolType.MICROWAVE,
                    timeMinutes = 2,
                    timeSeconds = 30
                )
            ),
            memo = "맛있는 테스트 블록"
        )
        blockRepo.saveFoodBlock(testBlock)
        val blocksResult = blockRepo.fetchFoodBlocks()
        assertTrue("Blocks fetch failed", blocksResult.isSuccess)
        val createdBlock = blocksResult.getOrThrow().find { it.name == "소고기 볶음 블록" }
        assertNotNull("Created block should exist", createdBlock)

        // 7. Test Meal Record and Preset
        val mealRepo = com.dahee.blockbyblock.data.repository.NetworkMealRecordRepository()
        val randomDay = (10..28).random()
        val today = "2026-09-$randomDay"
        val mealBlock = com.dahee.blockbyblock.domain.model.MealBlockItem(
            instanceId = "inst_1",
            blockId = createdBlock!!.id,
            blockName = createdBlock.name,
            blockColorHex = createdBlock.blockColorHex,
            moldCapacityMl = createdBlock.moldCapacityMl,
            moldCellCount = createdBlock.moldCellCount
        )
        val dayRecord = com.dahee.blockbyblock.domain.model.DayMealRecord(
            id = "day_$today",
            dateString = today,
            lunch = com.dahee.blockbyblock.domain.model.MealSlotRecord(
                mealType = com.dahee.blockbyblock.domain.model.MealType.LUNCH,
                blocks = listOf(mealBlock, mealBlock), // Multiple instances of identical block (same blockId)
                memo = "점심 맛있게 먹기",
                customTitle = "단백질 든든 식단"
            )
        )
        mealRepo.saveMealRecord(dayRecord)
        val dailyFetch = mealRepo.fetchDailyMeal(today)
        assertTrue("Fetch daily meal failed", dailyFetch.isSuccess)
        val fetchedLunch = dailyFetch.getOrThrow()?.lunch
        assertNotNull(fetchedLunch)
        assertEquals("Both block instances should be restored", 2, fetchedLunch!!.blocks.size)

        // Test Preset with duplicate blocks
        val testPreset = com.dahee.blockbyblock.domain.model.MealPreset(
            id = "",
            name = "다이어트 세트",
            blocks = listOf(mealBlock, mealBlock),
            memo = "주말용"
        )
        mealRepo.saveMealPreset(testPreset)
        val presetsResult = mealRepo.fetchMealPresets()
        assertTrue("Fetch presets failed", presetsResult.isSuccess)
        val createdPreset = presetsResult.getOrThrow().find { it.name == "다이어트 세트" }
        assertNotNull("Created preset should exist", createdPreset)
        assertEquals(2, createdPreset!!.blocks.size)

        // Clean up preset & meal
        mealRepo.deleteMealPreset(createdPreset!!.id)
        mealRepo.deleteMealRecord(today)

        // Delete block
        blockRepo.deleteFoodBlock(createdBlock.id)

        // 8. Logout
        val logoutResult = authService.logout()
        assertTrue("Logout failed", logoutResult.isSuccess)
        assertTrue(!TokenStorage.isAuthenticated)
    }
}
