package com.dahee.blockbyblock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.font.FontWeight
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
import com.dahee.blockbyblock.domain.model.ProfileAvatarType
import com.dahee.blockbyblock.domain.model.UserProfile
import com.dahee.blockbyblock.presentation.auth.AuthScreen
import com.dahee.blockbyblock.presentation.block.BlockInventoryScreen
import com.dahee.blockbyblock.presentation.block.BlockViewModel
import com.dahee.blockbyblock.presentation.equipment.EquipmentScreen
import com.dahee.blockbyblock.presentation.equipment.EquipmentViewModel
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
    // Auth & Onboarding State
    var isLoggedIn by remember { mutableStateOf(false) }
    var hasCompletedOnboarding by remember { mutableStateOf(false) }
    var userProfile by remember {
        mutableStateOf(
            UserProfile()
        )
    }

    var currentTab by remember { mutableStateOf(NavTab.MEAL_PLAN) }
    var isManagingEquipment by remember { mutableStateOf(false) }
    var tutorialStep by remember { mutableStateOf(TutorialStep.WELCOME_PROFILE) }

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

    var currentLanguage by remember { mutableStateOf(getPlatform().defaultLanguage) }

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

    var showSaveEquipmentConfirmDialog by remember { mutableStateOf(false) }

    CompositionLocalProvider(
        LocalAppLanguage provides currentLanguage,
        LocalStrings provides getStrings(currentLanguage)
    ) {
        val strings = LocalStrings.current

        // Save Equipment & Continue Confirm Dialog
        if (showSaveEquipmentConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showSaveEquipmentConfirmDialog = false },
                title = {
                    Text(
                        text = strings.tutorialSaveEquipmentConfirmTitle,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                },
                text = {
                    Text(
                        text = strings.tutorialSaveEquipmentConfirmMsg,
                        fontSize = 14.sp,
                        color = AppColors.TextSecondary
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showSaveEquipmentConfirmDialog = false
                            equipmentViewModel.onSaveAllEquipment(
                                onSuccess = {
                                    tutorialStep = TutorialStep.INVENTORY_SETUP
                                    isManagingEquipment = false
                                    currentTab = NavTab.INVENTORY
                                }
                            )
                        }
                    ) {
                        Text(
                            text = strings.tutorialSaveAndNextBtn,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Primary
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showSaveEquipmentConfirmDialog = false }
                    ) {
                        Text(
                            text = strings.cancel,
                            fontSize = 14.sp,
                            color = AppColors.TextSecondary
                        )
                    }
                },
                containerColor = AppColors.Surface,
                shape = RoundedCornerShape(16.dp)
            )
        }

        BlockByBlockTheme {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.Background),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 500.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // 1. Unauthenticated: Auth Screen (Login / Sign Up)
                    if (!isLoggedIn) {
                        AuthScreen(
                            onLoginSuccess = { email ->
                                isLoggedIn = true
                                userProfile = userProfile.copy(email = email)
                                if (!hasCompletedOnboarding) {
                                    tutorialStep = TutorialStep.WELCOME_PROFILE
                                } else {
                                    tutorialStep = TutorialStep.COMPLETED
                                    currentTab = NavTab.MEAL_PLAN
                                }
                            },
                            onSignUpSuccess = { email ->
                                isLoggedIn = true
                                hasCompletedOnboarding = false
                                userProfile = userProfile.copy(email = email)
                                tutorialStep = TutorialStep.WELCOME_PROFILE
                            },
                            onSkip = {
                                isLoggedIn = true
                                userProfile = userProfile.copy(email = "guest@blockbyblock.com")
                                if (!hasCompletedOnboarding) {
                                    tutorialStep = TutorialStep.WELCOME_PROFILE
                                } else {
                                    tutorialStep = TutorialStep.COMPLETED
                                    currentTab = NavTab.MEAL_PLAN
                                }
                            }
                        )
                    }
                    // 2. First-time Onboarding: Full Screen Welcome & Nickname Setup
                    else if (tutorialStep == TutorialStep.WELCOME_PROFILE) {
                        WelcomeProfileScreen(
                            onStart = { name ->
                                userProfile = userProfile.copy(nickname = name)
                                hasCompletedOnboarding = true
                                tutorialStep = TutorialStep.EQUIPMENT_SETUP
                                isManagingEquipment = true
                                currentTab = NavTab.INVENTORY
                            }
                        )
                    }
                    // 3. Main Authenticated App Flow
                    else {
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
                                            if (equipmentViewModel.validateEquipmentSelection()) {
                                                showSaveEquipmentConfirmDialog = true
                                            }
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
                                    hasCompletedOnboarding = true
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
                                        onNavigateBack = if (tutorialStep == TutorialStep.EQUIPMENT_SETUP) null else {
                                            { isManagingEquipment = false }
                                        },
                                        onSaved = {
                                            if (tutorialStep == TutorialStep.EQUIPMENT_SETUP) {
                                                tutorialStep = TutorialStep.INVENTORY_SETUP
                                                currentTab = NavTab.INVENTORY
                                            }
                                            isManagingEquipment = false
                                        }
                                    )
                                } else {
                                    when (currentTab) {
                                        NavTab.MEAL_PLAN -> MealPlanScreen(
                                            viewModel = mealPlanViewModel,
                                            onCreateBlockClick = {
                                                isManagingEquipment = false
                                                currentTab = NavTab.BLOCK
                                                blockViewModel.onOpenCreateScreen()
                                            }
                                        )
                                        NavTab.BLOCK -> BlockInventoryScreen(
                                            viewModel = blockViewModel,
                                            onNavigateToInventory = {
                                                isManagingEquipment = false
                                                currentTab = NavTab.INVENTORY
                                            },
                                            onNavigateToEquipment = {
                                                equipmentViewModel.onOpenDirectSetup()
                                                isManagingEquipment = true
                                            }
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
                                            userProfile = userProfile,
                                            onProfileChange = { userProfile = it },
                                            onLanguageChange = { currentLanguage = it },
                                            onNavigateToEquipment = {
                                                equipmentViewModel.onOpenDirectSetup()
                                                isManagingEquipment = true
                                            },
                                            onRestartTutorial = {
                                                isManagingEquipment = false
                                                tutorialStep = TutorialStep.WELCOME_PROFILE
                                            },
                                            onLogout = {
                                                isLoggedIn = false
                                            }
                                        )
                                    }
                                }
                            }

                            if (!(isManagingEquipment && tutorialStep == TutorialStep.EQUIPMENT_SETUP)) {
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
        }
    }
}