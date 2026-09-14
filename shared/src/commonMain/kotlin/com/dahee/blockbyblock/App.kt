package com.dahee.blockbyblock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import com.dahee.blockbyblock.core.i18n.LocalAppLanguage
import com.dahee.blockbyblock.core.i18n.LocalStrings
import com.dahee.blockbyblock.core.i18n.getStrings
import com.dahee.blockbyblock.core.notification.PushNotificationManager
import com.dahee.blockbyblock.core.theme.AppColors
import com.dahee.blockbyblock.core.theme.BlockByBlockTheme
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
import com.dahee.blockbyblock.presentation.navigation.AppSideNav
import com.dahee.blockbyblock.presentation.navigation.NavTab
import com.dahee.blockbyblock.presentation.tutorial.TutorialGuideBanner
import com.dahee.blockbyblock.presentation.tutorial.TutorialStep
import com.dahee.blockbyblock.presentation.tutorial.WelcomeProfileScreen
import com.dahee.blockbyblock.data.repository.NetworkNotificationRepository
import com.dahee.blockbyblock.presentation.notification.NotificationScreen
import com.dahee.blockbyblock.presentation.notification.NotificationViewModel


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
    var isViewingNotifications by remember { mutableStateOf(false) }
    var tutorialStep by remember { mutableStateOf(TutorialStep.WELCOME_PROFILE) }


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

    val notificationRepository = remember { NetworkNotificationRepository() }
    val notificationViewModel = remember { NotificationViewModel(notificationRepository) }
    val notificationUiState by notificationViewModel.uiState.collectAsState()
    val unreadCount = notificationUiState.unreadCount

    androidx.compose.runtime.DisposableEffect(Unit) {
        com.dahee.blockbyblock.data.remote.ApiClient.setAuthFailureHandler { _ ->
            isLoggedIn = false
            userProfile = UserProfile()
            notificationViewModel.reset()
        }
        onDispose {
            com.dahee.blockbyblock.data.remote.ApiClient.setAuthFailureHandler(null)
        }
    }

    val ingredientUiState by ingredientViewModel.uiState.collectAsState()
    val blockUiState by blockViewModel.uiState.collectAsState()


    val hasAddedIngredient = ingredientUiState.registeredIngredients.isNotEmpty()
    val hasCreatedBlock = blockUiState.blocks.isNotEmpty()

    fun fetchMainAppData(includeNotifications: Boolean = false) {
        coroutineScope.launch {
            PushNotificationManager.syncDeviceOrTimezone(deviceApiService, userApiService)
            equipmentRepository.fetchEquipments()
            ingredientRepository.fetchIngredients()
            foodBlockRepository.fetchFoodBlocks()
            mealRecordRepository.fetchWeeklyMeals(com.dahee.blockbyblock.core.utils.getCurrentDateIso())
            mealRecordRepository.fetchMealPresets()
            if (includeNotifications && TokenStorage.isAuthenticated && !TokenStorage.getAccessToken().isNullOrBlank()) {
                notificationViewModel.fetchUnreadCount()
            }
        }
    }

    // Auto-restore login session on startup / page refresh
    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (TokenStorage.isAuthenticated && !TokenStorage.getAccessToken().isNullOrBlank()) {
            val meResult = userApiService.getMe()
            meResult.onSuccess { userRes ->
                val savedLocalLang = TokenStorage.getUserLang()
                val targetLang = if (savedLocalLang != null) {
                    try {
                        com.dahee.blockbyblock.core.i18n.AppLanguage.valueOf(savedLocalLang)
                    } catch (_: Throwable) {
                        try {
                            com.dahee.blockbyblock.core.i18n.AppLanguage.valueOf(userRes.lang)
                        } catch (_: Throwable) {
                            getPlatform().defaultLanguage
                        }
                    }
                } else {
                    val detected = getPlatform().defaultLanguage
                    if (detected == com.dahee.blockbyblock.core.i18n.AppLanguage.EN && userRes.lang == "KO") {
                        coroutineScope.launch {
                            userApiService.updateProfile(
                                UpdateProfileRequest(
                                    nickname = userRes.nickname,
                                    avatarType = userRes.avatarType,
                                    lang = detected.name
                                )
                            )
                        }
                        TokenStorage.setUserLang(detected.name)
                        detected
                    } else {
                        try {
                            val serverEnum = com.dahee.blockbyblock.core.i18n.AppLanguage.valueOf(userRes.lang)
                            TokenStorage.setUserLang(serverEnum.name)
                            serverEnum
                        } catch (_: Throwable) {
                            detected
                        }
                    }
                }
                currentLanguage = targetLang
                equipmentViewModel.setLanguage(targetLang)
                ingredientViewModel.setLanguage(targetLang)
                mealPlanViewModel.setLanguage(targetLang)
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
                    fetchMainAppData(includeNotifications = false)
                } else {
                    tutorialStep = TutorialStep.COMPLETED
                    currentTab = NavTab.MEAL_PLAN
                    fetchMainAppData(includeNotifications = true)
                }
            }.onFailure {
                val refresh = TokenStorage.getRefreshToken()
                if (!refresh.isNullOrBlank()) {
                    val refreshRes = authApiService.refreshToken(refresh)
                    refreshRes.onSuccess {
                        val retryMe = userApiService.getMe()
                        retryMe.onSuccess { userRes ->
                            val savedLocalLang = TokenStorage.getUserLang()
                            val targetLang = if (savedLocalLang != null) {
                                try {
                                    com.dahee.blockbyblock.core.i18n.AppLanguage.valueOf(savedLocalLang)
                                } catch (_: Throwable) {
                                    try {
                                        com.dahee.blockbyblock.core.i18n.AppLanguage.valueOf(userRes.lang)
                                    } catch (_: Throwable) {
                                        getPlatform().defaultLanguage
                                    }
                                }
                            } else {
                                val detected = getPlatform().defaultLanguage
                                if (detected == com.dahee.blockbyblock.core.i18n.AppLanguage.EN && userRes.lang == "KO") {
                                    coroutineScope.launch {
                                        userApiService.updateProfile(
                                            UpdateProfileRequest(
                                                nickname = userRes.nickname,
                                                avatarType = userRes.avatarType,
                                                lang = detected.name
                                            )
                                        )
                                    }
                                    TokenStorage.setUserLang(detected.name)
                                    detected
                                } else {
                                    try {
                                        val serverEnum = com.dahee.blockbyblock.core.i18n.AppLanguage.valueOf(userRes.lang)
                                        TokenStorage.setUserLang(serverEnum.name)
                                        serverEnum
                                    } catch (_: Throwable) {
                                        detected
                                    }
                                }
                            }
                            currentLanguage = targetLang
                            equipmentViewModel.setLanguage(targetLang)
                            ingredientViewModel.setLanguage(targetLang)
                            mealPlanViewModel.setLanguage(targetLang)
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
                                fetchMainAppData(includeNotifications = false)
                            } else {
                                tutorialStep = TutorialStep.COMPLETED
                                currentTab = NavTab.MEAL_PLAN
                                fetchMainAppData(includeNotifications = true)
                            }
                        }.onFailure {
                            com.dahee.blockbyblock.data.remote.ApiClient.clearAuthTokens()
                            notificationViewModel.reset()
                            isLoggedIn = false
                        }
                    }.onFailure {
                        com.dahee.blockbyblock.data.remote.ApiClient.clearAuthTokens()
                        notificationViewModel.reset()
                        isLoggedIn = false
                    }
                } else {
                    com.dahee.blockbyblock.data.remote.ApiClient.clearAuthTokens()
                    notificationViewModel.reset()
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
            if (TokenStorage.isAuthenticated && !TokenStorage.getAccessToken().isNullOrBlank()) {
                notificationViewModel.fetchUnreadCount()
            }
        }
    }

    // Sync language with equipmentViewModel, ingredientViewModel and mealPlanViewModel
    androidx.compose.runtime.LaunchedEffect(currentLanguage) {
        equipmentViewModel.setLanguage(currentLanguage)
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
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.Background),
                contentAlignment = Alignment.Center
            ) {
                val currentDensity = LocalDensity.current
                val isWideScreen = maxWidth >= 840.dp

                // Mobile responsive density scaling:
                // Base reference width is 390dp (standard iPhone baseline).
                // On narrower mobile devices (e.g. 360dp on Samsung Galaxy devices),
                // scale down density proportionally so UI components and typography fit comfortably
                // without unwanted overflowing or vertical scrolling.
                val responsiveScale = if (!isWideScreen && maxWidth < 390.dp && maxWidth > 0.dp) {
                    (maxWidth.value / 390f).coerceIn(0.85f, 1.0f)
                } else {
                    1.0f
                }

                val responsiveDensity = remember(currentDensity, responsiveScale) {
                    Density(
                        density = currentDensity.density * responsiveScale,
                        fontScale = currentDensity.fontScale
                    )
                }

                CompositionLocalProvider(LocalDensity provides responsiveDensity) {
                    var isSideNavExpanded by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = if (isLoggedIn && tutorialStep != TutorialStep.WELCOME_PROFILE && isWideScreen) 10000.dp else 768.dp)
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
                            currentLanguage = currentLanguage,
                            onLanguageChange = { newLang ->
                                currentLanguage = newLang
                                TokenStorage.setUserLang(newLang.name)
                                equipmentViewModel.setLanguage(newLang)
                                ingredientViewModel.setLanguage(newLang)
                                mealPlanViewModel.setLanguage(newLang)
                            },
                            onLoginSuccess = { profile ->
                                isLoggedIn = true
                                userProfile = profile
                                hasCompletedOnboarding = profile.onboardingCompleted
                                val targetLang = TokenStorage.getUserLang()?.let { langStr ->
                                    try {
                                        com.dahee.blockbyblock.core.i18n.AppLanguage.valueOf(langStr)
                                    } catch (_: Throwable) { null }
                                } ?: getPlatform().defaultLanguage
                                currentLanguage = targetLang
                                TokenStorage.setUserLang(targetLang.name)
                                equipmentViewModel.setLanguage(targetLang)
                                ingredientViewModel.setLanguage(targetLang)
                                mealPlanViewModel.setLanguage(targetLang)

                                if (!profile.onboardingCompleted) {
                                    tutorialStep = TutorialStep.WELCOME_PROFILE
                                    fetchMainAppData(includeNotifications = false)
                                } else {
                                    tutorialStep = TutorialStep.COMPLETED
                                    currentTab = NavTab.MEAL_PLAN
                                    fetchMainAppData(includeNotifications = true)
                                }
                            },
                            onSignUpSuccess = { profile ->
                                isLoggedIn = true
                                hasCompletedOnboarding = false
                                userProfile = profile
                                tutorialStep = TutorialStep.WELCOME_PROFILE
                                TokenStorage.setUserLang(currentLanguage.name)
                                equipmentViewModel.setLanguage(currentLanguage)
                                ingredientViewModel.setLanguage(currentLanguage)
                                mealPlanViewModel.setLanguage(currentLanguage)
                                fetchMainAppData(includeNotifications = false)
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
                                            avatarType = userProfile.avatarType.name,
                                            lang = currentLanguage.name
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
                        val renderMainScreens: @Composable () -> Unit = {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(AppColors.Background)
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
                                    if (TokenStorage.isAuthenticated && !TokenStorage.getAccessToken().isNullOrBlank()) {
                                        notificationViewModel.fetchUnreadCount()
                                    }
                                }
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                            ) {
                                if (isViewingNotifications) {
                                    NotificationScreen(
                                        viewModel = notificationViewModel,
                                        onNavigateBack = {
                                            isViewingNotifications = false
                                            notificationViewModel.fetchUnreadCount()
                                        },
                                        onNavigateToBlock = { blockId ->
                                            isViewingNotifications = false
                                            isManagingEquipment = false
                                            currentTab = NavTab.BLOCK
                                            coroutineScope.launch {
                                                var foundBlock = foodBlockRepository.getFoodBlocks().find { it.id == blockId.toString() }
                                                if (foundBlock == null) {
                                                    val res = foodBlockRepository.fetchFoodBlocks()
                                                    if (res.isSuccess) {
                                                        foundBlock = res.getOrNull()?.find { it.id == blockId.toString() }
                                                    }
                                                }
                                                if (foundBlock != null) {
                                                    blockViewModel.onOpenEditScreen(foundBlock)
                                                }
                                            }
                                            notificationViewModel.fetchUnreadCount()

                                        }
                                    )
                                } else if (isManagingEquipment) {
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
                                            } else {
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
                                            },
                                            unreadCount = unreadCount,
                                            onNavigateToNotifications = {
                                                isViewingNotifications = true
                                                notificationViewModel.refresh(showLoading = true)
                                            }
                                        )

                                        NavTab.BLOCK -> BlockInventoryScreen(
                                            viewModel = blockViewModel,
                                            ingredientViewModel = ingredientViewModel,
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
                                                notificationViewModel.reset()
                                                isLoggedIn = false
                                            },
                                            onDeleteAccount = {
                                                coroutineScope.launch {
                                                    com.dahee.blockbyblock.core.notification.PushNotificationManager.onLogout(deviceApiService)
                                                    val res = userApiService.withdraw()
                                                    res.onSuccess {
                                                        com.dahee.blockbyblock.data.remote.ApiClient.clearAuthTokens()
                                                        notificationViewModel.reset()
                                                        isLoggedIn = false
                                                        userProfile = UserProfile()
                                                    }.onFailure { err ->
                                                        if (err is com.dahee.blockbyblock.data.remote.error.ApiError && err.status == 401) {
                                                            com.dahee.blockbyblock.data.remote.ApiClient.clearAuthTokens()
                                                            notificationViewModel.reset()
                                                            isLoggedIn = false
                                                            userProfile = UserProfile()
                                                        } else {
                                                            com.dahee.blockbyblock.data.remote.ApiClient.showToast(err.message ?: strings.authErrorNetwork)
                                                        }
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                            }
                        }

                        if (isWideScreen) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                AppSideNav(
                                    currentTab = currentTab,
                                    onTabSelected = {
                                        currentTab = it
                                        isManagingEquipment = false
                                        isViewingNotifications = false
                                    },
                                    onLogoClick = {
                                        currentTab = NavTab.MEAL_PLAN
                                        isManagingEquipment = false
                                        isViewingNotifications = false
                                        blockViewModel.onCloseCreateScreen()
                                    },
                                    isExpanded = isSideNavExpanded,
                                    onToggleExpand = { isSideNavExpanded = !isSideNavExpanded },
                                    userProfile = userProfile
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .safeDrawingPadding(),
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .widthIn(max = 960.dp)
                                            .fillMaxWidth()
                                    ) {
                                        renderMainScreens()
                                    }
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(AppColors.Background)
                                    .safeDrawingPadding()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                ) {
                                    renderMainScreens()
                                }

                                if (!isViewingNotifications && !(isManagingEquipment && tutorialStep == TutorialStep.EQUIPMENT_SETUP)) {
                                    AppBottomNav(
                                        currentTab = currentTab,
                                        onTabSelected = {
                                            currentTab = it
                                            isManagingEquipment = false
                                            isViewingNotifications = false
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
}
}