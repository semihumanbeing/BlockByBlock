package com.dahee.blockbyblock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
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
import com.dahee.blockbyblock.core.i18n.LocalAppLanguage
import com.dahee.blockbyblock.core.i18n.LocalStrings
import com.dahee.blockbyblock.core.i18n.getStrings
import com.dahee.blockbyblock.core.notification.PushNotificationManager
import com.dahee.blockbyblock.core.theme.AppColors
import com.dahee.blockbyblock.core.theme.BlockByBlockTheme
import com.dahee.blockbyblock.data.repository.InMemoryEquipmentRepository
import com.dahee.blockbyblock.data.repository.InMemoryFoodBlockRepository
import com.dahee.blockbyblock.data.repository.InMemoryIngredientRepository
import com.dahee.blockbyblock.data.repository.InMemoryMealRecordRepository
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

import androidx.compose.runtime.rememberCoroutineScope
import com.dahee.blockbyblock.data.remote.TokenStorage
import com.dahee.blockbyblock.data.remote.dto.UpdateOnboardingRequest
import com.dahee.blockbyblock.data.remote.dto.UpdateProfileRequest
import com.dahee.blockbyblock.data.remote.service.AuthApiService
import com.dahee.blockbyblock.data.remote.service.UserApiService
import kotlinx.coroutines.launch

@Composable
fun App() {
    val coroutineScope = rememberCoroutineScope()
    val authApiService = remember { AuthApiService() }
    val userApiService = remember { UserApiService() }
    val deviceApiService = remember { com.dahee.blockbyblock.data.remote.service.DeviceApiService() }

    // Auth & Onboarding State
    var isLoggedIn by remember { mutableStateOf(false) }
    var isCheckingAuth by remember { mutableStateOf(TokenStorage.isAuthenticated) }
    var hasCompletedOnboarding by remember { mutableStateOf(false) }
    var userProfile by remember {
        mutableStateOf(
            UserProfile()
        )
    }

    var currentTab by remember { mutableStateOf(NavTab.MEAL_PLAN) }
    var isManagingEquipment by remember { mutableStateOf(false) }
    var tutorialStep by remember { mutableStateOf(TutorialStep.WELCOME_PROFILE) }

    androidx.compose.runtime.DisposableEffect(Unit) {
        com.dahee.blockbyblock.data.remote.ApiClient.setAuthFailureHandler { _ ->
            isLoggedIn = false
            userProfile = UserProfile()
        }
        onDispose {
            com.dahee.blockbyblock.data.remote.ApiClient.setAuthFailureHandler(null)
        }
    }

    val initialLang = remember {
        val savedLang = TokenStorage.getUserLang()
        if (savedLang != null) {
            try {
                com.dahee.blockbyblock.core.i18n.AppLanguage.valueOf(savedLang)
            } catch (_: Throwable) {
                getPlatform().defaultLanguage
            }
        } else {
            getPlatform().defaultLanguage
        }
    }
    var currentLanguage by remember { mutableStateOf(initialLang) }

    val equipmentRepository = remember { com.dahee.blockbyblock.data.repository.NetworkEquipmentRepository() }
    val equipmentViewModel = remember { EquipmentViewModel(equipmentRepository, initialLanguage = initialLang) }

    val ingredientRepository = remember { com.dahee.blockbyblock.data.repository.NetworkIngredientRepository() }
    val ingredientViewModel = remember { IngredientViewModel(ingredientRepository, initialLanguage = initialLang) }

    val foodBlockRepository = remember { com.dahee.blockbyblock.data.repository.NetworkFoodBlockRepository() }
    val blockViewModel = remember {
        BlockViewModel(
            foodBlockRepository = foodBlockRepository,
            ingredientRepository = ingredientRepository,
            equipmentRepository = equipmentRepository
        )
    }

    val mealRecordRepository = remember { com.dahee.blockbyblock.data.repository.NetworkMealRecordRepository() }
    val mealPlanViewModel = remember {
        MealPlanViewModel(
            mealRecordRepository = mealRecordRepository,
            foodBlockRepository = foodBlockRepository,
            initialLanguage = initialLang
        )
    }

    val ingredientUiState by ingredientViewModel.uiState.collectAsState()
    val blockUiState by blockViewModel.uiState.collectAsState()

    val hasAddedIngredient = ingredientUiState.registeredIngredients.isNotEmpty()
    val hasCreatedBlock = blockUiState.blocks.isNotEmpty()

    // Auto-restore login session on startup / page refresh
    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (TokenStorage.isAuthenticated) {
            val meResult = userApiService.getMe()
            meResult.onSuccess { userRes ->
                try {
                    val langEnum = com.dahee.blockbyblock.core.i18n.AppLanguage.valueOf(userRes.lang)
                    currentLanguage = langEnum
                    equipmentViewModel.setLanguage(langEnum)
                    ingredientViewModel.setLanguage(langEnum)
                    mealPlanViewModel.setLanguage(langEnum)
                } catch (_: Throwable) {}
                val avatar = try {
                    com.dahee.blockbyblock.domain.model.ProfileAvatarType.valueOf(userRes.avatarType)
                } catch (_: Throwable) {
                    com.dahee.blockbyblock.domain.model.ProfileAvatarType.PERSON
                }
                val profile = UserProfile(
                    id = userRes.id,
                    nickname = userRes.nickname,
                    avatarType = avatar,
                    email = userRes.email,
                    onboardingCompleted = userRes.onboardingCompleted
                )
                userProfile = profile
                hasCompletedOnboarding = profile.onboardingCompleted
                isLoggedIn = true
                if (!profile.onboardingCompleted) {
                    tutorialStep = TutorialStep.WELCOME_PROFILE
                } else {
                    tutorialStep = TutorialStep.COMPLETED
                    currentTab = NavTab.MEAL_PLAN
                }
                coroutineScope.launch { equipmentRepository.fetchEquipments() }
                coroutineScope.launch { ingredientRepository.fetchIngredients() }
                coroutineScope.launch { foodBlockRepository.fetchFoodBlocks() }
                coroutineScope.launch { mealRecordRepository.fetchWeeklyMeals(com.dahee.blockbyblock.core.utils.getCurrentDateIso()) }
                coroutineScope.launch { mealRecordRepository.fetchMealPresets() }
                coroutineScope.launch { com.dahee.blockbyblock.core.notification.PushNotificationManager.syncDeviceOrTimezone(deviceApiService, userApiService) }
            }.onFailure {
                val refresh = TokenStorage.getRefreshToken()
                if (!refresh.isNullOrBlank()) {
                    val refreshRes = authApiService.refreshToken(refresh)
                    refreshRes.onSuccess {
                        val retryMe = userApiService.getMe()
                        retryMe.onSuccess { userRes ->
                            try {
                                val langEnum = com.dahee.blockbyblock.core.i18n.AppLanguage.valueOf(userRes.lang)
                                currentLanguage = langEnum
                                equipmentViewModel.setLanguage(langEnum)
                                ingredientViewModel.setLanguage(langEnum)
                                mealPlanViewModel.setLanguage(langEnum)
                            } catch (_: Throwable) {}
                            val avatar = try {
                                com.dahee.blockbyblock.domain.model.ProfileAvatarType.valueOf(userRes.avatarType)
                            } catch (_: Throwable) {
                                com.dahee.blockbyblock.domain.model.ProfileAvatarType.PERSON
                            }
                            val profile = UserProfile(
                                id = userRes.id,
                                nickname = userRes.nickname,
                                avatarType = avatar,
                                email = userRes.email,
                                onboardingCompleted = userRes.onboardingCompleted
                            )
                            userProfile = profile
                            hasCompletedOnboarding = profile.onboardingCompleted
                            isLoggedIn = true
                            if (!profile.onboardingCompleted) {
                                tutorialStep = TutorialStep.WELCOME_PROFILE
                            } else {
                                tutorialStep = TutorialStep.COMPLETED
                                currentTab = NavTab.MEAL_PLAN
                            }
                            coroutineScope.launch { equipmentRepository.fetchEquipments() }
                            coroutineScope.launch { ingredientRepository.fetchIngredients() }
                            coroutineScope.launch { foodBlockRepository.fetchFoodBlocks() }
                            coroutineScope.launch { mealRecordRepository.fetchWeeklyMeals(com.dahee.blockbyblock.core.utils.getCurrentDateIso()) }
                            coroutineScope.launch { mealRecordRepository.fetchMealPresets() }
                            coroutineScope.launch { com.dahee.blockbyblock.core.notification.PushNotificationManager.syncDeviceOrTimezone(deviceApiService, userApiService) }
                        }.onFailure {
                            TokenStorage.clearTokens()
                            isLoggedIn = false
                        }
                    }.onFailure {
                        TokenStorage.clearTokens()
                        isLoggedIn = false
                    }
                } else {
                    TokenStorage.clearTokens()
                    isLoggedIn = false
                }
            }
            isCheckingAuth = false
        }
    }

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

    // Sync language with ingredientViewModel and mealPlanViewModel
    androidx.compose.runtime.LaunchedEffect(currentLanguage) {
        ingredientViewModel.setLanguage(currentLanguage)
        mealPlanViewModel.setLanguage(currentLanguage)
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
                        .fillMaxHeight()
                        .widthIn(max = 768.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // 0. Checking saved auth session
                    if (isCheckingAuth) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(AppColors.Background),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.CircularProgressIndicator(
                                color = AppColors.Primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    // 1. Unauthenticated: Auth Screen (Login / Sign Up)
                    else if (!isLoggedIn) {
                        AuthScreen(
                            authApiService = authApiService,
                            onLoginSuccess = { profile ->
                                isLoggedIn = true
                                userProfile = profile
                                hasCompletedOnboarding = profile.onboardingCompleted
                                TokenStorage.getUserLang()?.let { langStr ->
                                    try {
                                        currentLanguage = com.dahee.blockbyblock.core.i18n.AppLanguage.valueOf(langStr)
                                    } catch (_: Throwable) {}
                                }
                                coroutineScope.launch {
                                    PushNotificationManager.syncDeviceOrTimezone(deviceApiService, userApiService)
                                    equipmentRepository.fetchEquipments()
                                    ingredientRepository.fetchIngredients()
                                    foodBlockRepository.fetchFoodBlocks()
                                    mealRecordRepository.fetchWeeklyMeals(com.dahee.blockbyblock.core.utils.getCurrentDateIso())
                                    mealRecordRepository.fetchMealPresets()
                                }
                                if (!profile.onboardingCompleted) {
                                    tutorialStep = TutorialStep.WELCOME_PROFILE
                                } else {
                                    tutorialStep = TutorialStep.COMPLETED
                                    currentTab = NavTab.MEAL_PLAN
                                }
                            },
                            onSignUpSuccess = { profile ->
                                isLoggedIn = true
                                hasCompletedOnboarding = false
                                userProfile = profile
                                tutorialStep = TutorialStep.WELCOME_PROFILE
                                coroutineScope.launch {
                                    PushNotificationManager.syncDeviceOrTimezone(deviceApiService, userApiService)
                                }
                            }
                        )
                    }
                    // 2. First-time Onboarding: Full Screen Welcome & Nickname Setup
                    else if (tutorialStep == TutorialStep.WELCOME_PROFILE) {
                        WelcomeProfileScreen(
                            onStart = { name ->
                                userProfile = userProfile.copy(nickname = name, onboardingCompleted = true)
                                hasCompletedOnboarding = true
                                tutorialStep = TutorialStep.EQUIPMENT_SETUP
                                equipmentViewModel.onOpenDirectSetup()
                                isManagingEquipment = true
                                currentTab = NavTab.INVENTORY
                                coroutineScope.launch {
                                    userApiService.updateProfile(
                                        UpdateProfileRequest(
                                            nickname = name,
                                            avatarType = userProfile.avatarType.name
                                        )
                                    )
                                    userApiService.updateOnboarding(
                                        UpdateOnboardingRequest(onboardingCompleted = true)
                                    )
                                }
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
                                                isManagingEquipment = false
                                            }
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
                                                equipmentViewModel.onOpenListScreen()
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
                                            onProfileChange = { updated ->
                                                userProfile = updated
                                                coroutineScope.launch {
                                                    userApiService.updateProfile(
                                                        UpdateProfileRequest(
                                                            nickname = updated.nickname,
                                                            avatarType = updated.avatarType.name
                                                        )
                                                    )
                                                }
                                            },
                                            onLanguageChange = { newLang ->
                                                currentLanguage = newLang
                                                TokenStorage.setUserInfo(TokenStorage.getUserId(), newLang.name)
                                                equipmentViewModel.setLanguage(newLang)
                                                ingredientViewModel.setLanguage(newLang)
                                                mealPlanViewModel.setLanguage(newLang)
                                                coroutineScope.launch {
                                                    userApiService.updateProfile(
                                                        UpdateProfileRequest(
                                                            nickname = userProfile.nickname,
                                                            avatarType = userProfile.avatarType.name,
                                                            lang = newLang.name
                                                        )
                                                    )
                                                }
                                            },
                                            onNavigateToEquipment = {
                                                equipmentViewModel.onOpenListScreen()
                                                isManagingEquipment = true
                                            },
                                            onRestartTutorial = {
                                                isManagingEquipment = false
                                                tutorialStep = TutorialStep.WELCOME_PROFILE
                                            },
                                            onLogout = {
                                                coroutineScope.launch {
                                                    com.dahee.blockbyblock.core.notification.PushNotificationManager.onLogout(deviceApiService)
                                                    authApiService.logout()
                                                }
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