package com.dahee.blockbyblock.presentation.auth

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import blockbyblock.shared.generated.resources.Res
import blockbyblock.shared.generated.resources.food_block_3d_1x4_green
import blockbyblock.shared.generated.resources.food_block_3d_2x2_yellow
import blockbyblock.shared.generated.resources.food_block_3d_2x4_orange
import blockbyblock.shared.generated.resources.food_block_3d_3x4_red
import com.dahee.blockbyblock.core.i18n.AppLanguage
import com.dahee.blockbyblock.core.i18n.AppStrings
import com.dahee.blockbyblock.core.i18n.LocalStrings
import com.dahee.blockbyblock.data.remote.error.ApiError
import com.dahee.blockbyblock.data.remote.error.ErrorCode
import com.dahee.blockbyblock.core.theme.AppColors
import com.dahee.blockbyblock.core.ui.AppButton
import com.dahee.blockbyblock.core.ui.AppCard
import com.dahee.blockbyblock.core.ui.AppTextField
import com.dahee.blockbyblock.core.ui.ButtonVariant
import org.jetbrains.compose.resources.painterResource

import androidx.compose.runtime.rememberCoroutineScope
import com.dahee.blockbyblock.data.remote.dto.LoginRequest
import com.dahee.blockbyblock.data.remote.dto.PasswordResetConfirmRequest
import com.dahee.blockbyblock.data.remote.dto.PasswordResetRequest
import com.dahee.blockbyblock.data.remote.dto.SignUpRequest
import com.dahee.blockbyblock.data.remote.dto.SocialLoginRequest
import com.dahee.blockbyblock.data.remote.service.AuthApiService
import com.dahee.blockbyblock.domain.model.ProfileAvatarType
import com.dahee.blockbyblock.domain.model.UserProfile
import kotlinx.coroutines.launch

enum class AuthMode {
    LOGIN,
    SIGN_UP,
    RESET_PASSWORD
}

enum class ResetPasswordStep {
    REQUEST_CODE,
    CONFIRM_CODE,
    SUCCESS
}

private sealed interface AuthErrorState {
    data class Form(val throwable: Throwable, val isLogin: Boolean) : AuthErrorState
    data object SocialNetwork : AuthErrorState
    data object SocialFailed : AuthErrorState
    data object UserNotFound : AuthErrorState
    data object InvalidVerificationCode : AuthErrorState
    data object VerificationCodeExpired : AuthErrorState
    data class Custom(val message: String) : AuthErrorState
}

@Composable
fun AuthScreen(
    onLoginSuccess: (user: UserProfile) -> Unit,
    onSignUpSuccess: (user: UserProfile) -> Unit,
    authApiService: AuthApiService = remember { AuthApiService() },
    googleAuthProvider: GoogleAuthProvider = rememberGoogleAuthProvider(),
    currentLanguage: AppLanguage = AppLanguage.KO,
    onLanguageChange: (AppLanguage) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf(AuthMode.LOGIN) }
    val strings = LocalStrings.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    var authErrorState by remember { mutableStateOf<AuthErrorState?>(null) }
    val errorMessage = authErrorState?.let { state ->
        when (state) {
            is AuthErrorState.Form -> formatAuthError(state.throwable, isLogin = state.isLogin, strings)
            AuthErrorState.SocialNetwork -> strings.authErrorNetwork
            AuthErrorState.SocialFailed -> strings.authErrorSocialLogin
            AuthErrorState.UserNotFound -> strings.authErrorUserNotFound
            AuthErrorState.InvalidVerificationCode -> strings.authErrorInvalidVerificationCode
            AuthErrorState.VerificationCodeExpired -> strings.authErrorVerificationCodeExpired
            is AuthErrorState.Custom -> state.message
        }
    }

    // Password Reset States
    var resetStep by remember { mutableStateOf(ResetPasswordStep.REQUEST_CODE) }
    var resetEmailInput by remember { mutableStateOf("") }
    var resetCodeInput by remember { mutableStateOf("") }
    var newPasswordInput by remember { mutableStateOf("") }
    var newPasswordConfirmInput by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordConfirmVisible by remember { mutableStateOf(false) }
    var remainingSeconds by remember { mutableStateOf(600) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var isResendingCode by remember { mutableStateOf(false) }
    var codeSentToastVisible by remember { mutableStateOf(false) }

    LaunchedEffect(mode) {
        authErrorState = null
        if (mode != AuthMode.RESET_PASSWORD) {
            isTimerRunning = false
        }
    }

    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            while (remainingSeconds > 0) {
                kotlinx.coroutines.delay(1000L)
                remainingSeconds--
            }
            isTimerRunning = false
        }
    }

    // Form inputs
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordConfirmInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordConfirmVisible by remember { mutableStateOf(false) }

    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val passwordConfirmFocusRequester = remember { FocusRequester() }

    // Terms agreement states
    var agreeTermsService by remember { mutableStateOf(false) }
    var agreeTermsPrivacy by remember { mutableStateOf(false) }

    val agreeAll = agreeTermsService && agreeTermsPrivacy

    // Validation checks for Sign Up
    val isEmailValid = emailInput.contains("@") && emailInput.substringAfter("@").contains(".") && !emailInput.endsWith(".")
    val showEmailError = mode == AuthMode.SIGN_UP && emailInput.isNotBlank() && !isEmailValid

    val hasNumber = passwordInput.any { it.isDigit() }
    val hasLower = passwordInput.any { it.isLowerCase() }
    val isLengthValid = passwordInput.length >= 8
    val isPasswordPolicyMet = hasNumber && hasLower && isLengthValid
    val showPasswordPolicyError = mode == AuthMode.SIGN_UP && passwordInput.isNotBlank() && !isPasswordPolicyMet

    val isPasswordConfirmMatched = passwordConfirmInput.isNotBlank() && passwordConfirmInput == passwordInput
    val showPasswordConfirmMismatchError = mode == AuthMode.SIGN_UP && passwordConfirmInput.isNotBlank() && !isPasswordConfirmMatched

    val isFormValid = if (mode == AuthMode.LOGIN) {
        emailInput.isNotBlank() && passwordInput.isNotBlank()
    } else {
        isEmailValid && isPasswordPolicyMet && isPasswordConfirmMatched && agreeAll
    }

    val submitForm = {
        if (isFormValid && !isLoading) {
            scope.launch {
                isLoading = true
                authErrorState = null
                if (mode == AuthMode.LOGIN) {
                    val res = authApiService.login(LoginRequest(emailInput.trim(), passwordInput))
                    isLoading = false
                    res.onSuccess { loginRes ->
                        val avatar = try {
                            ProfileAvatarType.valueOf(loginRes.user.avatarType)
                        } catch (_: Exception) {
                            ProfileAvatarType.PERSON
                        }
                        onLoginSuccess(
                            UserProfile(
                                id = loginRes.user.id,
                                nickname = loginRes.user.nickname,
                                avatarType = avatar,
                                email = loginRes.user.email,
                                onboardingCompleted = loginRes.user.onboardingCompleted
                            )
                        )
                    }.onFailure { err ->
                        authErrorState = AuthErrorState.Form(err, isLogin = true)
                    }
                } else {
                    val nickname = emailInput.substringBefore("@")
                    val res = authApiService.signUp(SignUpRequest(emailInput.trim(), passwordInput, nickname))
                    isLoading = false
                    res.onSuccess { signUpRes ->
                        onSignUpSuccess(
                            UserProfile(
                                id = signUpRes.userId,
                                nickname = nickname,
                                avatarType = ProfileAvatarType.PERSON,
                                email = emailInput.trim(),
                                onboardingCompleted = false
                            )
                        )
                    }.onFailure { err ->
                        authErrorState = AuthErrorState.Form(err, isLogin = false)
                    }
                }
            }
        }
    }
    val isResetEmailValid = resetEmailInput.contains("@") && resetEmailInput.substringAfter("@").contains(".") && !resetEmailInput.endsWith(".")
    val isStep1Valid = resetEmailInput.isNotBlank() && isResetEmailValid

    val hasNewNumber = newPasswordInput.any { it.isDigit() }
    val hasNewLower = newPasswordInput.any { it.isLowerCase() }
    val isNewLengthValid = newPasswordInput.length >= 8
    val isNewPasswordPolicyMet = hasNewNumber && hasNewLower && isNewLengthValid
    val isNewPasswordConfirmMatched = newPasswordConfirmInput.isNotBlank() && newPasswordConfirmInput == newPasswordInput
    val isStep2Valid = resetCodeInput.trim().length == 6 && isNewPasswordPolicyMet && isNewPasswordConfirmMatched && remainingSeconds > 0

    val submitResetRequest = {
        if (isStep1Valid && !isLoading) {
            scope.launch {
                isLoading = true
                authErrorState = null
                val res = authApiService.requestPasswordReset(resetEmailInput.trim())
                isLoading = false
                res.onSuccess {
                    remainingSeconds = 600
                    isTimerRunning = true
                    resetStep = ResetPasswordStep.CONFIRM_CODE
                    codeSentToastVisible = true
                }.onFailure { err ->
                    authErrorState = if (err is ApiError && (err.isUserNotFound() || err.status == 404)) {
                        AuthErrorState.UserNotFound
                    } else {
                        AuthErrorState.Form(err, isLogin = false)
                    }
                }
            }
        }
    }

    val resendResetCode = {
        if (!isResendingCode && !isLoading) {
            scope.launch {
                isResendingCode = true
                val res = authApiService.requestPasswordReset(resetEmailInput.trim())
                isResendingCode = false
                res.onSuccess {
                    remainingSeconds = 600
                    isTimerRunning = true
                    authErrorState = null
                    codeSentToastVisible = true
                }.onFailure { err ->
                    authErrorState = if (err is ApiError && (err.isUserNotFound() || err.status == 404)) {
                        AuthErrorState.UserNotFound
                    } else {
                        AuthErrorState.Form(err, isLogin = false)
                    }
                }
            }
        }
    }

    val submitResetConfirm = {
        if (remainingSeconds <= 0) {
            authErrorState = AuthErrorState.VerificationCodeExpired
        } else if (isStep2Valid && !isLoading) {
            scope.launch {
                isLoading = true
                authErrorState = null
                val res = authApiService.confirmPasswordReset(
                    PasswordResetConfirmRequest(
                        email = resetEmailInput.trim(),
                        code = resetCodeInput.trim(),
                        newPassword = newPasswordInput
                    )
                )
                isLoading = false
                res.onSuccess {
                    isTimerRunning = false
                    resetStep = ResetPasswordStep.SUCCESS
                }.onFailure { err ->
                    authErrorState = if (err is ApiError && (err.isInvalidVerificationCode() || err.status == 400)) {
                        AuthErrorState.InvalidVerificationCode
                    } else {
                        AuthErrorState.Form(err, isLogin = false)
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .safeDrawingPadding()
            .imePadding()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
            },
        contentAlignment = Alignment.Center
    ) {
        // Top right language toggle
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LanguageToggleButton(
                currentLanguage = currentLanguage,
                onLanguageChange = onLanguageChange
            )
        }

        // Center Content Container
        Column(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Top Header: Back button for SIGN_UP and RESET_PASSWORD mode
            if (mode == AuthMode.SIGN_UP || mode == AuthMode.RESET_PASSWORD) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AppColors.SurfaceVariant)
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = {
                                    if (mode == AuthMode.RESET_PASSWORD && resetStep == ResetPasswordStep.CONFIRM_CODE) {
                                        resetStep = ResetPasswordStep.REQUEST_CODE
                                    } else {
                                        mode = AuthMode.LOGIN
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.back,
                            tint = AppColors.TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = if (mode == AuthMode.SIGN_UP) strings.authSignUpTitle else strings.authPasswordResetTitle,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                }
            } else {
                // Stacked 3D Food Blocks Hero Visual for LOGIN mode
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Bottom Layer: 3x4 Red Block (Large)
                    Image(
                        painter = painterResource(Res.drawable.food_block_3d_3x4_red),
                        contentDescription = "Red Food Block",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(125.dp)
                            .offset(x = (-8).dp, y = 28.dp)
                    )

                    // Middle Layer: 2x4 Orange Block (Medium)
                    Image(
                        painter = painterResource(Res.drawable.food_block_3d_2x4_orange),
                        contentDescription = "Orange Food Block",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(110.dp)
                            .offset(x = 15.dp, y = (-4).dp)
                    )

                    // Upper Layer Left: 1x4 Green Block (Small)
                    Image(
                        painter = painterResource(Res.drawable.food_block_3d_1x4_green),
                        contentDescription = "Green Food Block",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(88.dp)
                            .offset(x = (-30).dp, y = (-32).dp)
                    )

                    // Upper Layer Right: 2x2 Yellow Block (Mini)
                    Image(
                        painter = painterResource(Res.drawable.food_block_3d_2x2_yellow),
                        contentDescription = "Yellow Food Block",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(72.dp)
                            .offset(x = 28.dp, y = (-44).dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // App Brand Title
                Text(
                    text = strings.authLoginTitle,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AppColors.TextPrimary,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = strings.authLoginSubtitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(22.dp))
            }

            if (mode != AuthMode.RESET_PASSWORD) {
                // Google Social Action Button
                GoogleSocialButton(
                    text = if (mode == AuthMode.LOGIN) strings.authGoogleLoginBtn else strings.authGoogleSignUpBtn,
                    onClick = {
                        if (!isLoading) {
                            scope.launch {
                                isLoading = true
                                authErrorState = null
                                when (val authResult = googleAuthProvider.signIn()) {
                                    is GoogleAuthResult.Cancelled -> {
                                        isLoading = false
                                    }
                                    is GoogleAuthResult.Failure -> {
                                        isLoading = false
                                        authErrorState = if (authResult.message.contains("network", ignoreCase = true) ||
                                            authResult.message.contains("connect", ignoreCase = true)
                                        ) {
                                            AuthErrorState.SocialNetwork
                                        } else {
                                            AuthErrorState.SocialFailed
                                        }
                                    }
                                    is GoogleAuthResult.Success -> {
                                        val res = authApiService.socialLogin(
                                            SocialLoginRequest(
                                                provider = "GOOGLE",
                                                idToken = authResult.idToken
                                            )
                                        )
                                        isLoading = false
                                        res.onSuccess { loginRes ->
                                            val avatar = try {
                                                ProfileAvatarType.valueOf(loginRes.user.avatarType)
                                            } catch (_: Exception) {
                                                ProfileAvatarType.PERSON
                                            }
                                            val profile = UserProfile(
                                                id = loginRes.user.id,
                                                nickname = loginRes.user.nickname,
                                                avatarType = avatar,
                                                email = loginRes.user.email,
                                                onboardingCompleted = loginRes.user.onboardingCompleted
                                            )
                                            if (mode == AuthMode.LOGIN) onLoginSuccess(profile) else onSignUpSuccess(profile)
                                        }.onFailure { err ->
                                            authErrorState = if (err is ApiError && (err.code == ErrorCode.NETWORK_ERROR || err.code == ErrorCode.TIMEOUT_ERROR)) {
                                                AuthErrorState.SocialNetwork
                                            } else {
                                                AuthErrorState.SocialFailed
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Divider: [--- or ---]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = AppColors.Border.copy(alpha = 0.6f)
                    )
                    Text(
                        text = strings.authOrDivider,
                        fontSize = 12.sp,
                        color = AppColors.TextMuted,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = AppColors.Border.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (mode == AuthMode.RESET_PASSWORD) {
                PasswordResetCard(
                    step = resetStep,
                    email = resetEmailInput,
                    onEmailChange = {
                        resetEmailInput = it
                        if (authErrorState != null) authErrorState = null
                    },
                    isEmailValid = isResetEmailValid,
                    code = resetCodeInput,
                    onCodeChange = {
                        resetCodeInput = it
                        if (authErrorState != null) authErrorState = null
                        if (codeSentToastVisible) codeSentToastVisible = false
                    },
                    newPassword = newPasswordInput,
                    onNewPasswordChange = {
                        newPasswordInput = it
                        if (authErrorState != null) authErrorState = null
                    },
                    newPasswordConfirm = newPasswordConfirmInput,
                    onNewPasswordConfirmChange = {
                        newPasswordConfirmInput = it
                        if (authErrorState != null) authErrorState = null
                    },
                    newPasswordVisible = newPasswordVisible,
                    onToggleNewPasswordVisible = { newPasswordVisible = !newPasswordVisible },
                    newPasswordConfirmVisible = newPasswordConfirmVisible,
                    onToggleNewPasswordConfirmVisible = { newPasswordConfirmVisible = !newPasswordConfirmVisible },
                    isNewPasswordPolicyMet = isNewPasswordPolicyMet,
                    isNewPasswordConfirmMatched = isNewPasswordConfirmMatched,
                    remainingSeconds = remainingSeconds,
                    isLoading = isLoading,
                    isResendingCode = isResendingCode,
                    codeSentToastVisible = codeSentToastVisible,
                    errorMessage = errorMessage,
                    strings = strings,
                    onRequestCode = submitResetRequest,
                    onResendCode = resendResetCode,
                    onConfirmReset = submitResetConfirm,
                    onBackToLogin = {
                        emailInput = resetEmailInput
                        passwordInput = ""
                        authErrorState = null
                        mode = AuthMode.LOGIN
                    }
                )
            } else {
                // Main Form Box
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    padding = 18.dp
                ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    if (errorMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFFEBEE))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = errorMessage,
                                color = Color(0xFFC62828),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Email Field
                    Column {
                        Text(
                            text = strings.authEmailLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        AppTextField(
                            value = emailInput,
                            onValueChange = {
                                emailInput = it
                                if (authErrorState != null) authErrorState = null
                            },
                            placeholder = strings.authEmailPlaceholder,
                            focusRequester = emailFocusRequester,
                            inputModifier = Modifier.onPreviewKeyEvent { keyEvent ->
                                if (keyEvent.key == Key.Tab && keyEvent.type == KeyEventType.KeyDown) {
                                    if (!keyEvent.isShiftPressed) {
                                        passwordFocusRequester.requestFocus()
                                        true
                                    } else {
                                        false
                                    }
                                } else {
                                    false
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { passwordFocusRequester.requestFocus() }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (showEmailError) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = strings.authErrorInvalidEmail,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFE53935)
                            )
                        }
                    }

                    // Password Field
                    Column {
                        Text(
                            text = strings.authPasswordLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(contentAlignment = Alignment.CenterEnd) {
                            AppTextField(
                                value = passwordInput,
                                onValueChange = {
                                    passwordInput = it
                                    if (authErrorState != null) authErrorState = null
                                },
                                placeholder = strings.authPasswordPlaceholder,
                                focusRequester = passwordFocusRequester,
                                inputModifier = Modifier.onPreviewKeyEvent { keyEvent ->
                                    if (keyEvent.key == Key.Tab && keyEvent.type == KeyEventType.KeyDown) {
                                        if (keyEvent.isShiftPressed) {
                                            emailFocusRequester.requestFocus()
                                            true
                                        } else if (mode == AuthMode.SIGN_UP) {
                                            passwordConfirmFocusRequester.requestFocus()
                                            true
                                        } else {
                                            false
                                        }
                                    } else {
                                        false
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = if (mode == AuthMode.LOGIN) ImeAction.Done else ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = {
                                        if (mode == AuthMode.SIGN_UP) {
                                            passwordConfirmFocusRequester.requestFocus()
                                        }
                                    },
                                    onDone = {
                                        focusManager.clearFocus()
                                        if (isFormValid && mode == AuthMode.LOGIN) {
                                            submitForm()
                                        }
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Box(
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .size(24.dp)
                                    .pointerHoverIcon(PointerIcon.Hand)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                        onClick = { passwordVisible = !passwordVisible }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password",
                                    tint = AppColors.TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        if (showPasswordPolicyError) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = strings.authErrorPasswordPolicy,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFE53935)
                            )
                        } else if (mode == AuthMode.SIGN_UP) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (isPasswordPolicyMet) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = AppColors.Primary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                Text(
                                    text = strings.authPasswordPolicyHint,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isPasswordPolicyMet) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isPasswordPolicyMet) AppColors.Primary else AppColors.TextMuted
                                )
                            }
                        }
                    }

                    if (mode == AuthMode.LOGIN) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = strings.authForgotPasswordLink,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.TextSecondary,
                                modifier = Modifier
                                    .pointerHoverIcon(PointerIcon.Hand)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                        onClick = {
                                            resetEmailInput = emailInput.trim()
                                            resetStep = ResetPasswordStep.REQUEST_CODE
                                            mode = AuthMode.RESET_PASSWORD
                                            authErrorState = null
                                        }
                                    )
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }

                    // Password Confirm Field (Sign Up only)
                    if (mode == AuthMode.SIGN_UP) {
                        Column {
                            Text(
                                text = strings.authPasswordConfirmLabel,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(contentAlignment = Alignment.CenterEnd) {
                                AppTextField(
                                    value = passwordConfirmInput,
                                    onValueChange = {
                                        passwordConfirmInput = it
                                        if (authErrorState != null) authErrorState = null
                                    },
                                    placeholder = strings.authPasswordConfirmPlaceholder,
                                    focusRequester = passwordConfirmFocusRequester,
                                    inputModifier = Modifier.onPreviewKeyEvent { keyEvent ->
                                        if (keyEvent.key == Key.Tab && keyEvent.type == KeyEventType.KeyDown) {
                                            if (keyEvent.isShiftPressed) {
                                                passwordFocusRequester.requestFocus()
                                                true
                                            } else {
                                                false
                                            }
                                        } else {
                                            false
                                        }
                                    },
                                    visualTransformation = if (passwordConfirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            focusManager.clearFocus()
                                            if (isFormValid && mode == AuthMode.SIGN_UP) {
                                                submitForm()
                                            }
                                        }
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(
                                    modifier = Modifier.padding(end = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (isPasswordConfirmMatched) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Matched",
                                            tint = AppColors.Primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .pointerHoverIcon(PointerIcon.Hand)
                                            .clickable(
                                                indication = null,
                                                interactionSource = remember { MutableInteractionSource() },
                                                onClick = { passwordConfirmVisible = !passwordConfirmVisible }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (passwordConfirmVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle password confirm",
                                            tint = AppColors.TextMuted,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            if (showPasswordConfirmMismatchError) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = strings.authErrorPasswordMismatch,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFE53935)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Terms Agreement Box
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppColors.SurfaceVariant.copy(alpha = 0.5f))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Master Agreement
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .pointerHoverIcon(PointerIcon.Hand)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                        onClick = {
                                            val target = !agreeAll
                                            agreeTermsService = target
                                            agreeTermsPrivacy = target
                                        }
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CheckBoxIcon(checked = agreeAll)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = strings.authTermsAgreeAll,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.TextPrimary
                                )
                            }

                            HorizontalDivider(
                                color = AppColors.Border.copy(alpha = 0.5f),
                                thickness = 0.5.dp
                            )

                            // Terms of Service
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .pointerHoverIcon(PointerIcon.Hand)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                        onClick = { agreeTermsService = !agreeTermsService }
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CheckBoxIcon(checked = agreeTermsService)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "[${strings.authTermsRequiredBadge}] ${strings.authTermsService}",
                                    fontSize = 12.sp,
                                    color = AppColors.TextSecondary
                                )
                            }

                            // Privacy Policy
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .pointerHoverIcon(PointerIcon.Hand)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                        onClick = { agreeTermsPrivacy = !agreeTermsPrivacy }
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CheckBoxIcon(checked = agreeTermsPrivacy)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "[${strings.authTermsRequiredBadge}] ${strings.authTermsPrivacy}",
                                    fontSize = 12.sp,
                                    color = AppColors.TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Primary Action Button
                    AppButton(
                        text = if (isLoading) "..." else if (mode == AuthMode.LOGIN) strings.authLoginBtn else strings.authSignUpBtn,
                        variant = ButtonVariant.PRIMARY,
                        enabled = isFormValid && !isLoading,
                        onClick = { submitForm() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        if (mode != AuthMode.RESET_PASSWORD) {
            Spacer(modifier = Modifier.height(20.dp))

            // Bottom Switcher: [No account? Sign Up] / [Have account? Log In]
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (mode == AuthMode.LOGIN) strings.authNoAccountPrompt else strings.authHasAccountPrompt,
                    fontSize = 13.sp,
                    color = AppColors.TextSecondary
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = if (mode == AuthMode.LOGIN) strings.authSignUpLink else strings.authLoginLink,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryDark,
                    modifier = Modifier
                        .pointerHoverIcon(PointerIcon.Hand)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = {
                                mode = if (mode == AuthMode.LOGIN) AuthMode.SIGN_UP else AuthMode.LOGIN
                            }
                        )
                        .padding(4.dp)
                )
            }
        }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun PasswordResetCard(
    step: ResetPasswordStep,
    email: String,
    onEmailChange: (String) -> Unit,
    isEmailValid: Boolean,
    code: String,
    onCodeChange: (String) -> Unit,
    newPassword: String,
    onNewPasswordChange: (String) -> Unit,
    newPasswordConfirm: String,
    onNewPasswordConfirmChange: (String) -> Unit,
    newPasswordVisible: Boolean,
    onToggleNewPasswordVisible: () -> Unit,
    newPasswordConfirmVisible: Boolean,
    onToggleNewPasswordConfirmVisible: () -> Unit,
    isNewPasswordPolicyMet: Boolean,
    isNewPasswordConfirmMatched: Boolean,
    remainingSeconds: Int,
    isLoading: Boolean,
    isResendingCode: Boolean,
    codeSentToastVisible: Boolean,
    errorMessage: String?,
    strings: AppStrings,
    onRequestCode: () -> Unit,
    onResendCode: () -> Unit,
    onConfirmReset: () -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        elevation = 3.dp,
        padding = 18.dp
    ) {
        when (step) {
            ResetPasswordStep.REQUEST_CODE -> {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = strings.authPasswordResetStep1Desc,
                        fontSize = 13.sp,
                        color = AppColors.TextSecondary,
                        lineHeight = 18.sp
                    )

                    if (errorMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFFEBEE))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = errorMessage,
                                color = Color(0xFFC62828),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Column {
                        Text(
                            text = strings.authEmailLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        AppTextField(
                            value = email,
                            onValueChange = onEmailChange,
                            placeholder = strings.authEmailPlaceholder,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (email.isNotBlank() && isEmailValid && !isLoading) {
                                        onRequestCode()
                                    }
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    AppButton(
                        text = if (isLoading) "..." else strings.authPasswordResetSendCodeBtn,
                        variant = ButtonVariant.PRIMARY,
                        enabled = email.isNotBlank() && isEmailValid && !isLoading,
                        onClick = onRequestCode,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = strings.authPasswordResetBackToLoginBtn,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.TextSecondary,
                            modifier = Modifier
                                .pointerHoverIcon(PointerIcon.Hand)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = onBackToLogin
                                )
                                .padding(vertical = 6.dp)
                        )
                    }
                }
            }

            ResetPasswordStep.CONFIRM_CODE -> {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = strings.authPasswordResetStep2Desc,
                        fontSize = 13.sp,
                        color = AppColors.TextSecondary,
                        lineHeight = 18.sp
                    )

                    // Target email box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(AppColors.SurfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = AppColors.TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = email,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.TextPrimary
                            )
                        }
                    }

                    // Timer & Resend Row
                    val minutes = (remainingSeconds / 60).toString().padStart(2, '0')
                    val seconds = (remainingSeconds % 60).toString().padStart(2, '0')
                    val isExpired = remainingSeconds <= 0

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = if (isExpired) Color(0xFFE53935) else AppColors.Primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${strings.authPasswordResetTimerLabel}: $minutes:$seconds",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isExpired) Color(0xFFE53935) else AppColors.Primary
                            )
                        }

                        Text(
                            text = if (isResendingCode) "..." else strings.authPasswordResetResendCodeBtn,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isResendingCode) AppColors.TextMuted else AppColors.PrimaryDark,
                            modifier = Modifier
                                .pointerHoverIcon(PointerIcon.Hand)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    enabled = !isResendingCode && !isLoading,
                                    onClick = onResendCode
                                )
                                .padding(4.dp)
                        )
                    }

                    if (codeSentToastVisible) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE8F5E9))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = strings.authPasswordResetCodeSentToast,
                                color = Color(0xFF2E7D32),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (errorMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFFEBEE))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = errorMessage,
                                color = Color(0xFFC62828),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Verification code field
                    Column {
                        Text(
                            text = strings.authVerificationCodeLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        AppTextField(
                            value = code,
                            onValueChange = { input ->
                                if (input.length <= 6) {
                                    onCodeChange(input.filter { it.isLetterOrDigit() }.uppercase())
                                }
                            },
                            placeholder = strings.authVerificationCodePlaceholder,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Ascii,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // New Password field
                    Column {
                        Text(
                            text = strings.authNewPasswordLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(contentAlignment = Alignment.CenterEnd) {
                            AppTextField(
                                value = newPassword,
                                onValueChange = onNewPasswordChange,
                                placeholder = strings.authNewPasswordPlaceholder,
                                visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Box(
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .size(24.dp)
                                    .pointerHoverIcon(PointerIcon.Hand)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                        onClick = onToggleNewPasswordVisible
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (newPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password",
                                    tint = AppColors.TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (isNewPasswordPolicyMet) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = AppColors.Primary,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Text(
                                text = strings.authPasswordPolicyHint,
                                fontSize = 11.5.sp,
                                fontWeight = if (isNewPasswordPolicyMet) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isNewPasswordPolicyMet) AppColors.Primary else AppColors.TextMuted
                            )
                        }
                    }

                    // New Password Confirm field
                    Column {
                        Text(
                            text = strings.authNewPasswordConfirmLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(contentAlignment = Alignment.CenterEnd) {
                            AppTextField(
                                value = newPasswordConfirm,
                                onValueChange = onNewPasswordConfirmChange,
                                placeholder = strings.authNewPasswordConfirmPlaceholder,
                                visualTransformation = if (newPasswordConfirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (code.length == 6 && isNewPasswordPolicyMet && isNewPasswordConfirmMatched && !isLoading && remainingSeconds > 0) {
                                            onConfirmReset()
                                        }
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.padding(end = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (isNewPasswordConfirmMatched) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Matched",
                                        tint = AppColors.Primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .pointerHoverIcon(PointerIcon.Hand)
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() },
                                            onClick = onToggleNewPasswordConfirmVisible
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (newPasswordConfirmVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password confirm",
                                        tint = AppColors.TextMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val canSubmit = code.length == 6 && isNewPasswordPolicyMet && isNewPasswordConfirmMatched && !isLoading && !isExpired
                    AppButton(
                        text = if (isLoading) "..." else strings.authPasswordResetSubmitBtn,
                        variant = ButtonVariant.PRIMARY,
                        enabled = canSubmit,
                        onClick = onConfirmReset,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = strings.authPasswordResetBackToLoginBtn,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.TextSecondary,
                            modifier = Modifier
                                .pointerHoverIcon(PointerIcon.Hand)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = onBackToLogin
                                )
                                .padding(vertical = 6.dp)
                        )
                    }
                }
            }

            ResetPasswordStep.SUCCESS -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = strings.authPasswordResetSuccessTitle,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )

                    Text(
                        text = strings.authPasswordResetSuccessDesc,
                        fontSize = 13.5.sp,
                        color = AppColors.TextSecondary,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    AppButton(
                        text = strings.authPasswordResetBackToLoginBtn,
                        variant = ButtonVariant.PRIMARY,
                        onClick = onBackToLogin,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun CheckBoxIcon(
    checked: Boolean,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(4.dp)
    Box(
        modifier = modifier
            .size(18.dp)
            .clip(shape)
            .background(if (checked) AppColors.Primary else Color.Transparent)
            .border(
                width = if (checked) 0.dp else 1.5.dp,
                color = if (checked) Color.Transparent else AppColors.TextMuted,
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

@Composable
private fun GoogleSocialButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .shadow(1.dp, shape, spotColor = AppColors.Shadow)
            .clip(shape)
            .background(Color.White)
            .border(0.8.dp, AppColors.Border, shape)
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Google 'G' Icon Visual (Vector/Block Style)
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(AppColors.PrimaryLight.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "G",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = AppColors.PrimaryDark
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
        }
    }
}

internal fun formatAuthError(
    err: Throwable,
    isLogin: Boolean,
    strings: AppStrings
): String {
    val msg = err.message ?: ""
    val lower = msg.lowercase()

    if (err is ApiError) {
        if (err.isAccountLocked() || err.code == ErrorCode.ACCOUNT_LOCKED ||
            (err.status == 429 && (lower.contains("account_locked") || lower.contains("locked") || err.code.contains("ACCOUNT_LOCKED", ignoreCase = true))) ||
            lower.contains("account_locked") || lower.contains("account is locked") || lower.contains("일시적으로 잠겼습니다") || lower.contains("일시 잠겼습니다") || lower.contains("잠겼습니다")
        ) {
            return strings.authErrorAccountLocked
        }
        if (err.isUserNotFound() || err.code == ErrorCode.USER_NOT_FOUND ||
            lower.contains("user_not_found") || lower.contains("가입되지 않은") || (err.status == 404 && (lower.contains("user") || lower.contains("resource")))
        ) {
            return strings.authErrorUserNotFound
        }
        if (err.isInvalidVerificationCode() || err.code == ErrorCode.INVALID_VERIFICATION_CODE ||
            lower.contains("invalid_verification_code") || lower.contains("인증번호가 일치하지") || (err.status == 400 && (lower.contains("code") || lower.contains("verification")))
        ) {
            return strings.authErrorInvalidVerificationCode
        }
        if (err.hasFieldErrors()) {
            val emailErr = err.getFieldErrorMessage("email")
            val pwErr = err.getFieldErrorMessage("password")
            val nickErr = err.getFieldErrorMessage("nickname")
            val rawField = emailErr ?: pwErr ?: nickErr
            if (rawField != null) {
                val rawLower = rawField.lowercase()
                if (rawLower.contains("already") || rawLower.contains("exist")) return strings.authErrorEmailAlreadyExists
                if (rawLower.contains("invalid") || rawLower.contains("format")) return strings.authErrorInvalidEmail
                return rawField
            }
        }
        if (err.status == 401 || err.code == ErrorCode.AUTHENTICATION_FAILED ||
            lower.contains("invalid email or password") || lower.contains("bad credential") || lower.contains("unauthorized")
        ) {
            return strings.authErrorInvalidCredentials
        }
        if (err.status == 409 || err.code == ErrorCode.EMAIL_ALREADY_EXISTS ||
            lower.contains("already exist") || lower.contains("duplicate")
        ) {
            return strings.authErrorEmailAlreadyExists
        }
        if (err.code == ErrorCode.NETWORK_ERROR ||
            err.code == ErrorCode.TIMEOUT_ERROR ||
            lower.contains("connect") || lower.contains("network") || lower.contains("timeout")
        ) {
            return strings.authErrorNetwork
        }
    }

    if (lower.contains("account_locked") || lower.contains("account locked") || lower.contains("일시적으로 잠겼습니다") || lower.contains("일시 잠겼습니다") || lower.contains("잠겼습니다")) {
        return strings.authErrorAccountLocked
    }
    if (lower.contains("user_not_found") || lower.contains("가입되지 않은") || lower.contains("user not found")) {
        return strings.authErrorUserNotFound
    }
    if (lower.contains("invalid_verification_code") || lower.contains("인증번호가 일치하지") || lower.contains("verification code")) {
        return strings.authErrorInvalidVerificationCode
    }
    if (lower.contains("invalid email or password") || lower.contains("bad credential") || lower.contains("incorrect password") || lower.contains("user not found")) {
        return strings.authErrorInvalidCredentials
    }
    if (lower.contains("already exist") || lower.contains("duplicate") || lower.contains("already registered")) {
        return strings.authErrorEmailAlreadyExists
    }
    if (lower.contains("network") || lower.contains("connect") || lower.contains("timeout")) {
        return strings.authErrorNetwork
    }

    return if (isLogin) strings.authErrorDefaultLogin else strings.authErrorDefaultSignUp
}

@Composable
fun LanguageToggleButton(
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    val isKorean = currentLanguage == AppLanguage.KO
    val indicatorOffset by animateDpAsState(
        targetValue = if (isKorean) 0.dp else 54.dp,
        animationSpec = tween(durationMillis = 200)
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.Surface)
            .border(1.dp, AppColors.Border, RoundedCornerShape(20.dp))
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onLanguageChange(currentLanguage.next()) }
            )
            .padding(3.dp)
    ) {
        // Sliding indicator pill
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(54.dp)
                .height(30.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(AppColors.Primary)
        )

        // Labels overlay
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(54.dp)
                    .height(30.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "한국어",
                    fontSize = 12.sp,
                    fontWeight = if (isKorean) FontWeight.Bold else FontWeight.Medium,
                    color = if (isKorean) Color.White else AppColors.TextSecondary
                )
            }
            Box(
                modifier = Modifier
                    .width(54.dp)
                    .height(30.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "EN",
                    fontSize = 12.sp,
                    fontWeight = if (!isKorean) FontWeight.Bold else FontWeight.Medium,
                    color = if (!isKorean) Color.White else AppColors.TextSecondary
                )
            }
        }
    }
}
