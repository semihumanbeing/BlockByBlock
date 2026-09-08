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
    private val deviceService = com.dahee.blockbyblock.data.remote.service.DeviceApiService()

    @Before
    fun setup() {
        ApiClient.baseUrl = "http://localhost:8000"
    }

    @Test
    fun testLoginAndProfileFlow() = runBlocking {
        val randomNum = (1000..9999).random()
        val testEmail = "test_user_$randomNum@example.com"
        val testPassword = "Password123!"

        // 1. Sign up
        val signUpResult = authService.signUp(
            com.dahee.blockbyblock.data.remote.dto.SignUpRequest(
                email = testEmail,
                password = testPassword,
                nickname = "테스터"
            )
        )
        assertTrue("SignUp failed: ${signUpResult.exceptionOrNull()?.message}", signUpResult.isSuccess)

        // 2. Get me
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

        // 3-1. Test User Timezone Update (PATCH /api/v1/users/me/timezone)
        val tzResult = userService.updateTimezone("America/New_York")
        assertTrue("updateTimezone failed: ${tzResult.exceptionOrNull()?.message}", tzResult.isSuccess)
        assertEquals("America/New_York", tzResult.getOrThrow().timezone)

        // 3-2. Test Mobile Device FCM Registration (POST /api/v1/devices)
        val testFcmToken = "test_fcm_token_${System.currentTimeMillis()}"
        val deviceReg = deviceService.registerDevice(
            fcmToken = testFcmToken,
            deviceType = "IOS",
            timezone = "America/New_York"
        )
        assertTrue("registerDevice failed: ${deviceReg.exceptionOrNull()?.message}", deviceReg.isSuccess)
        val registeredDevice = deviceReg.getOrThrow()
        assertEquals("IOS", registeredDevice.deviceType)
        assertTrue(registeredDevice.isActive)

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
        val testBlock1 = com.dahee.blockbyblock.domain.model.FoodBlock(
            id = "",
            name = "블록 1",
            moldId = mold.id,
            moldName = mold.name,
            moldCapacityMl = mold.displayCapacity,
            moldCellCount = mold.cellCount,
            moldColorHex = mold.moldColorHex,
            blockColorHex = "#FF7043",
            mainIngredients = listOf("소고기 안심"),
            subIngredients = listOf("양파", "당근"),
            quantity = 5,
            shelfLifeDays = 30,
            cookingInstructions = listOf(
                com.dahee.blockbyblock.domain.model.CookingInstruction(
                    toolType = com.dahee.blockbyblock.domain.model.CookingToolType.MICROWAVE,
                    timeMinutes = 2,
                    timeSeconds = 30
                )
            ),
            memo = "테스트 블록 1"
        )
        val testBlock2 = com.dahee.blockbyblock.domain.model.FoodBlock(
            id = "",
            name = "블록 2",
            moldId = mold.id,
            moldName = mold.name,
            moldCapacityMl = mold.displayCapacity,
            moldCellCount = mold.cellCount,
            moldColorHex = mold.moldColorHex,
            blockColorHex = "#4CAF50",
            mainIngredients = listOf("소고기 안심"),
            subIngredients = listOf("양파"),
            quantity = 5,
            shelfLifeDays = 30,
            cookingInstructions = listOf(
                com.dahee.blockbyblock.domain.model.CookingInstruction(
                    toolType = com.dahee.blockbyblock.domain.model.CookingToolType.MICROWAVE,
                    timeMinutes = 3,
                    timeSeconds = 0
                )
            ),
            memo = "테스트 블록 2"
        )
        blockRepo.saveFoodBlock(testBlock1)
        blockRepo.saveFoodBlock(testBlock2)
        val blocksResult = blockRepo.fetchFoodBlocks()
        assertTrue("Blocks fetch failed", blocksResult.isSuccess)
        val allCreatedBlocks = blocksResult.getOrThrow()
        val createdBlock1 = allCreatedBlocks.find { it.name == "블록 1" }
        val createdBlock2 = allCreatedBlocks.find { it.name == "블록 2" }
        assertNotNull("Created block 1 should exist", createdBlock1)
        assertNotNull("Created block 2 should exist", createdBlock2)
        assertNotNull("Block 1 expirationDate should be set", createdBlock1!!.expirationDate)
        assertNotNull("Block 1 daysRemaining should be calculated", createdBlock1.daysRemaining)
        assertTrue("Block 1 daysRemaining should be positive", createdBlock1.daysRemaining!! > 0)
        assertNotNull("Block 2 expirationDate should be set", createdBlock2!!.expirationDate)
        assertNotNull("Block 2 daysRemaining should be calculated", createdBlock2.daysRemaining)

        // 7. Test Meal Record and Preset with sequence 1, 2, 2, 1, 2
        val mealRepo = com.dahee.blockbyblock.data.repository.NetworkMealRecordRepository()
        val randomDay = (10..28).random()
        val today = "2026-09-$randomDay"
        val mealBlock1 = com.dahee.blockbyblock.domain.model.MealBlockItem(
            instanceId = "inst_b1",
            blockId = createdBlock1.id,
            blockName = createdBlock1.name,
            blockColorHex = createdBlock1.blockColorHex,
            moldCapacityMl = createdBlock1.moldCapacityMl,
            moldCellCount = createdBlock1.moldCellCount
        )
        val mealBlock2 = com.dahee.blockbyblock.domain.model.MealBlockItem(
            instanceId = "inst_b2",
            blockId = createdBlock2.id,
            blockName = createdBlock2.name,
            blockColorHex = createdBlock2.blockColorHex,
            moldCapacityMl = createdBlock2.moldCapacityMl,
            moldCellCount = createdBlock2.moldCellCount
        )

        // Input sequence: 1, 2, 2, 1, 2
        val inputSequence = listOf(mealBlock1, mealBlock2, mealBlock2, mealBlock1, mealBlock2)
        val expectedBlockIds = listOf(createdBlock1.id, createdBlock2.id, createdBlock2.id, createdBlock1.id, createdBlock2.id)
        val expectedBlockNames = listOf("블록 1", "블록 2", "블록 2", "블록 1", "블록 2")
        val expectedSortOrders = listOf(0, 1, 2, 3, 4)

        val dayRecord = com.dahee.blockbyblock.domain.model.DayMealRecord(
            id = "day_$today",
            dateString = today,
            lunch = com.dahee.blockbyblock.domain.model.MealSlotRecord(
                mealType = com.dahee.blockbyblock.domain.model.MealType.LUNCH,
                blocks = inputSequence,
                memo = "1, 2, 2, 1, 2 순서 배치 테스트",
                customTitle = "순서 보존 식단"
            )
        )
        mealRepo.saveMealRecord(dayRecord)

        val dailyFetch = mealRepo.fetchDailyMeal(today)
        assertTrue("Fetch daily meal failed", dailyFetch.isSuccess)
        val fetchedLunch = dailyFetch.getOrThrow()?.lunch
        assertNotNull(fetchedLunch)
        assertEquals("Should restore 5 block instances", 5, fetchedLunch!!.blocks.size)
        assertEquals("Block IDs should preserve 1, 2, 2, 1, 2 sequence", expectedBlockIds, fetchedLunch.blocks.map { it.blockId })
        assertEquals("Block names should preserve sequence", expectedBlockNames, fetchedLunch.blocks.map { it.blockName })
        assertEquals("Sort orders should be 0, 1, 2, 3, 4", expectedSortOrders, fetchedLunch.blocks.map { it.sortOrder })

        // Test Preset with duplicate blocks sequence 1, 2, 2, 1, 2
        val testPreset = com.dahee.blockbyblock.domain.model.MealPreset(
            id = "",
            name = "1_2_2_1_2 프리셋",
            blocks = inputSequence,
            memo = "순서 테스트용 프리셋"
        )
        mealRepo.saveMealPreset(testPreset)
        val presetsResult = mealRepo.fetchMealPresets()
        assertTrue("Fetch presets failed", presetsResult.isSuccess)
        val createdPreset = presetsResult.getOrThrow().find { it.name == "1_2_2_1_2 프리셋" }
        assertNotNull("Created preset should exist", createdPreset)
        assertEquals("Preset should contain 5 block instances", 5, createdPreset!!.blocks.size)
        assertEquals("Preset block IDs should preserve 1, 2, 2, 1, 2 sequence", expectedBlockIds, createdPreset.blocks.map { it.blockId })
        assertEquals("Preset block names should preserve sequence", expectedBlockNames, createdPreset.blocks.map { it.blockName })
        assertEquals("Preset sort orders should be 0, 1, 2, 3, 4", expectedSortOrders, createdPreset.blocks.map { it.sortOrder })

        // Clean up preset & meal
        mealRepo.deleteMealPreset(createdPreset.id)
        mealRepo.deleteMealRecord(today)

        // Delete blocks
        blockRepo.deleteFoodBlock(createdBlock1.id)
        blockRepo.deleteFoodBlock(createdBlock2.id)

        // 8. Test Mobile Device Unregister (DELETE /api/v1/devices?fcmToken=...)
        val unregResult = deviceService.unregisterDevice(testFcmToken)
        assertTrue("unregisterDevice failed: ${unregResult.exceptionOrNull()?.message}", unregResult.isSuccess)

        // 9. Logout
        val logoutResult = authService.logout()
        assertTrue("Logout failed", logoutResult.isSuccess)
        assertTrue(!TokenStorage.isAuthenticated)
    }
}
