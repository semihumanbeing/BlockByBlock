package com.dahee.blockbyblock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dahee.blockbyblock.core.i18n.AppLanguage
import com.dahee.blockbyblock.core.i18n.LocalAppLanguage
import com.dahee.blockbyblock.core.i18n.LocalStrings
import com.dahee.blockbyblock.core.i18n.getStrings
import com.dahee.blockbyblock.core.theme.AppColors
import com.dahee.blockbyblock.core.theme.BlockByBlockTheme
import com.dahee.blockbyblock.data.repository.InMemoryEquipmentRepository
import com.dahee.blockbyblock.data.repository.InMemoryFoodBlockRepository
import com.dahee.blockbyblock.data.repository.InMemoryIngredientRepository
import com.dahee.blockbyblock.data.repository.InMemoryMealRecordRepository
import com.dahee.blockbyblock.presentation.block.BlockInventoryScreen
import com.dahee.blockbyblock.presentation.block.BlockViewModel
import com.dahee.blockbyblock.presentation.equipment.EquipmentScreen
import com.dahee.blockbyblock.presentation.equipment.EquipmentViewModel
import com.dahee.blockbyblock.presentation.home.HomeScreen
import com.dahee.blockbyblock.presentation.inventory.IngredientViewModel
import com.dahee.blockbyblock.presentation.inventory.InventoryScreen
import com.dahee.blockbyblock.presentation.mealplan.MealPlanScreen
import com.dahee.blockbyblock.presentation.mealplan.MealPlanViewModel
import com.dahee.blockbyblock.presentation.me.MeScreen
import com.dahee.blockbyblock.presentation.navigation.AppBottomNav
import com.dahee.blockbyblock.presentation.navigation.NavTab
import com.dahee.blockbyblock.presentation.tutorial.TutorialGuideBanner
import com.dahee.blockbyblock.presentation.tutorial.TutorialStep
import com.dahee.blockbyblock.presentation.tutorial.WelcomeProfileScreen

@Composable
fun App() {
    var currentTab by remember { mutableStateOf(NavTab.MEAL_PLAN) }
    var isManagingEquipment by remember { mutableStateOf(false) }
    var tutorialStep by remember { mutableStateOf(TutorialStep.WELCOME_PROFILE) }
    var userNickname by remember { mutableStateOf("나만의 쉐프") }

    val equipmentRepository = remember { InMemoryEquipmentRepository() }
    val equipmentViewModel = remember { EquipmentViewModel(equipmentRepository) }

    val ingredientRepository = remember { InMemoryIngredientRepository() }
    val ingredientViewModel = remember { IngredientViewModel(ingredientRepository) }

    val foodBlockRepository = remember { InMemoryFoodBlockRepository() }
    val blockViewModel = remember {
        BlockViewModel(
            foodBlockRepository = foodBlockRepository,
            ingredientRepository = ingredientRepository,
            equipmentRepository = equipmentRepository
        )
    }

    val mealRecordRepository = remember { InMemoryMealRecordRepository() }
    val mealPlanViewModel = remember {
        MealPlanViewModel(
            mealRecordRepository = mealRecordRepository,
            foodBlockRepository = foodBlockRepository
        )
    }

    val ingredientUiState by ingredientViewModel.uiState.collectAsState()
    val blockUiState by blockViewModel.uiState.collectAsState()

    val hasAddedIngredient = ingredientUiState.registeredIngredients.isNotEmpty()
    val hasCreatedBlock = blockUiState.blocks.isNotEmpty()

    var currentLanguage by remember { mutableStateOf(AppLanguage.KO) }

    // Listen for meal slot save during tutorial
    androidx.compose.runtime.LaunchedEffect(mealPlanViewModel, tutorialStep) {
        mealPlanViewModel.onSlotSavedListener = {
            if (tutorialStep == TutorialStep.MEAL_PLAN) {
                tutorialStep = TutorialStep.CONGRATULATIONS
            }
        }
    }

    // Auto fade-out after celebration
    androidx.compose.runtime.LaunchedEffect(tutorialStep) {
        if (tutorialStep == TutorialStep.CONGRATULATIONS) {
            kotlinx.coroutines.delay(2800)
            tutorialStep = TutorialStep.COMPLETED
        }
    }

    CompositionLocalProvider(
        LocalAppLanguage provides currentLanguage,
        LocalStrings provides getStrings(currentLanguage)
    ) {
        val strings = LocalStrings.current

        BlockByBlockTheme {
            // 0. Full Screen Welcome & Nickname Setup (First Onboarding Step with pure background and stacked legos)
            if (tutorialStep == TutorialStep.WELCOME_PROFILE) {
                WelcomeProfileScreen(
                    onStart = { name ->
                        userNickname = name
                        tutorialStep = TutorialStep.EQUIPMENT_SETUP
                        isManagingEquipment = true
                        currentTab = NavTab.ME
                    },
                    onSkip = {
                        tutorialStep = TutorialStep.COMPLETED
                        isManagingEquipment = false
                        currentTab = NavTab.MEAL_PLAN
                    }
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppColors.Background)
                        .safeDrawingPadding()
                ) {
                // Top Interactive Tutorial Guide Banner (Active during tutorial)
                TutorialGuideBanner(
                    currentStep = tutorialStep,
                    hasAddedIngredient = hasAddedIngredient,
                    hasCreatedBlock = hasCreatedBlock,
                    onNextStep = {
                        when (tutorialStep) {
                            TutorialStep.EQUIPMENT_SETUP -> {
                                tutorialStep = TutorialStep.INVENTORY_SETUP
                                isManagingEquipment = false
                                currentTab = NavTab.INVENTORY
                            }
                            TutorialStep.INVENTORY_SETUP -> {
                                tutorialStep = TutorialStep.CREATE_BLOCK
                                isManagingEquipment = false
                                currentTab = NavTab.BLOCK
                                blockViewModel.onOpenCreateScreen()
                            }
                            TutorialStep.CREATE_BLOCK -> {
                                tutorialStep = TutorialStep.MEAL_PLAN
                                isManagingEquipment = false
                                currentTab = NavTab.MEAL_PLAN
                                blockViewModel.onCloseCreateScreen()
                            }
                            TutorialStep.MEAL_PLAN -> {
                                tutorialStep = TutorialStep.CONGRATULATIONS
                            }
                            else -> {}
                        }
                    },
                    onSkip = {
                        tutorialStep = TutorialStep.COMPLETED
                        isManagingEquipment = false
                    }
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    if (isManagingEquipment) {
                        EquipmentScreen(
                            viewModel = equipmentViewModel,
                            onNavigateBack = { isManagingEquipment = false }
                        )
                    } else {
                        when (currentTab) {
                            NavTab.MEAL_PLAN -> MealPlanScreen(
                                viewModel = mealPlanViewModel
                            )
                            NavTab.BLOCK -> BlockInventoryScreen(
                                viewModel = blockViewModel,
                                onNavigateToInventory = {
                                    isManagingEquipment = false
                                    currentTab = NavTab.INVENTORY
                                },
                                onNavigateToEquipment = { isManagingEquipment = true }
                            )
                            NavTab.INVENTORY -> InventoryScreen(
                                viewModel = ingredientViewModel,
                                onCookClick = {
                                    if (tutorialStep == TutorialStep.INVENTORY_SETUP) {
                                        tutorialStep = TutorialStep.CREATE_BLOCK
                                    }
                                    isManagingEquipment = false
                                    currentTab = NavTab.BLOCK
                                    blockViewModel.onOpenCreateScreen()
                                }
                            )
                            NavTab.ME -> MeScreen(
                                nickname = userNickname,
                                onNicknameChange = { userNickname = it },
                                onLanguageChange = { currentLanguage = it },
                                onNavigateToEquipment = { isManagingEquipment = true },
                                onRestartTutorial = {
                                    isManagingEquipment = false
                                    tutorialStep = TutorialStep.WELCOME_PROFILE
                                }
                            )
                        }
                    }
                }

                AppBottomNav(
                    currentTab = currentTab,
                    onTabSelected = {
                        currentTab = it
                        isManagingEquipment = false
                    }
                )
            }
        }
    }
}
}

@Composable
private fun PlaceholderTabScreen(title: String, desc: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = title,
                tint = AppColors.Primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
            Text(
                text = desc,
                fontSize = 14.sp,
                color = AppColors.TextSecondary,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}