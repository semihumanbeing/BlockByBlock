package com.dahee.blockbyblock.presentation.auth

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import blockbyblock.shared.generated.resources.Res
import blockbyblock.shared.generated.resources.food_block_3d_1x4_green
import blockbyblock.shared.generated.resources.food_block_3d_2x2_yellow
import blockbyblock.shared.generated.resources.food_block_3d_2x4_orange
import blockbyblock.shared.generated.resources.food_block_3d_3x4_red
import com.dahee.blockbyblock.core.i18n.LocalStrings
import com.dahee.blockbyblock.core.theme.AppColors
import com.dahee.blockbyblock.core.ui.AppButton
import com.dahee.blockbyblock.core.ui.AppCard
import com.dahee.blockbyblock.core.ui.AppTextField
import com.dahee.blockbyblock.core.ui.ButtonVariant
import org.jetbrains.compose.resources.painterResource

enum class AuthMode {
    LOGIN,
    SIGN_UP
}

@Composable
fun AuthScreen(
    onLoginSuccess: (email: String) -> Unit,
    onSignUpSuccess: (email: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf(AuthMode.LOGIN) }
    val strings = LocalStrings.current
    val focusManager = LocalFocusManager.current

    // Form inputs
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordConfirmInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordConfirmVisible by remember { mutableStateOf(false) }

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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .imePadding()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Top Header: Back button for SIGN_UP mode
            if (mode == AuthMode.SIGN_UP) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                                onClick = { mode = AuthMode.LOGIN }
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
                        text = strings.authSignUpTitle,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
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

            // Google Social Action Button
            GoogleSocialButton(
                text = if (mode == AuthMode.LOGIN) strings.authGoogleLoginBtn else strings.authGoogleSignUpBtn,
                onClick = {
                    val googleEmail = "user.google@blockbyblock.com"
                    if (mode == AuthMode.LOGIN) {
                        onLoginSuccess(googleEmail)
                    } else {
                        onSignUpSuccess(googleEmail)
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

            // Main Form Box
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                padding = 18.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
                            onValueChange = { emailInput = it },
                            placeholder = strings.authEmailPlaceholder,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
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
                                onValueChange = { passwordInput = it },
                                placeholder = strings.authPasswordPlaceholder,
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = if (mode == AuthMode.LOGIN) ImeAction.Done else ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        if (emailInput.isNotBlank() && passwordInput.isNotBlank() && mode == AuthMode.LOGIN) {
                                            onLoginSuccess(emailInput.trim())
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
                                    onValueChange = { passwordConfirmInput = it },
                                    placeholder = strings.authPasswordConfirmPlaceholder,
                                    visualTransformation = if (passwordConfirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = { focusManager.clearFocus() }
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
                    val isFormValid = if (mode == AuthMode.LOGIN) {
                        emailInput.isNotBlank() && passwordInput.isNotBlank()
                    } else {
                        isEmailValid && isPasswordPolicyMet && isPasswordConfirmMatched && agreeAll
                    }

                    AppButton(
                        text = if (mode == AuthMode.LOGIN) strings.authLoginBtn else strings.authSignUpBtn,
                        variant = ButtonVariant.PRIMARY,
                        enabled = isFormValid,
                        onClick = {
                            if (isFormValid) {
                                if (mode == AuthMode.LOGIN) {
                                    onLoginSuccess(emailInput.trim())
                                } else {
                                    onSignUpSuccess(emailInput.trim())
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

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

            Spacer(modifier = Modifier.height(20.dp))
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
