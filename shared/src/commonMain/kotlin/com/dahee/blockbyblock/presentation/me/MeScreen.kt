package com.dahee.blockbyblock.presentation.me

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dahee.blockbyblock.core.i18n.AppLanguage
import com.dahee.blockbyblock.core.i18n.LocalAppLanguage
import com.dahee.blockbyblock.core.i18n.LocalStrings
import com.dahee.blockbyblock.core.theme.AppColors
import com.dahee.blockbyblock.core.ui.AppCard
import com.dahee.blockbyblock.domain.model.UserProfile
import com.dahee.blockbyblock.presentation.me.components.BlockAvatarView
import com.dahee.blockbyblock.presentation.me.components.ProfileEditDialog

@Composable
fun MeScreen(
    userProfile: UserProfile = UserProfile(),
    onProfileChange: (UserProfile) -> Unit = {},
    onLanguageChange: (AppLanguage) -> Unit,
    onNavigateToEquipment: () -> Unit = {},
    onRestartTutorial: () -> Unit = {},
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val currentLang = LocalAppLanguage.current

    var isEditProfileDialogOpen by remember { mutableStateOf(false) }
    var isLogoutDialogOpen by remember { mutableStateOf(false) }
    var isDeleteAccountDialogOpen by remember { mutableStateOf(false) }
    var isRestartTutorialDialogOpen by remember { mutableStateOf(false) }

    // Edit Profile Modal Dialog
    if (isEditProfileDialogOpen) {
        ProfileEditDialog(
            initialProfile = userProfile,
            onSave = { updatedProfile ->
                onProfileChange(updatedProfile)
                isEditProfileDialogOpen = false
            },
            onDismiss = { isEditProfileDialogOpen = false }
        )
    }

    // Restart Tutorial Confirmation Dialog
    if (isRestartTutorialDialogOpen) {
        AlertDialog(
            onDismissRequest = { isRestartTutorialDialogOpen = false },
            title = {
                Text(
                    text = strings.tutorialRestartConfirmTitle,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
            },
            text = {
                Text(
                    text = strings.tutorialRestartConfirmMsg,
                    fontSize = 14.sp,
                    color = AppColors.TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        isRestartTutorialDialogOpen = false
                        onRestartTutorial()
                    }
                ) {
                    Text(
                        text = strings.done,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Primary
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { isRestartTutorialDialogOpen = false }) {
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

    // Logout Confirmation Dialog
    if (isLogoutDialogOpen) {
        AlertDialog(
            onDismissRequest = { isLogoutDialogOpen = false },
            title = {
                Text(
                    text = strings.profileLogoutConfirmTitle,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
            },
            text = {
                Text(
                    text = strings.profileLogoutConfirmMsg,
                    fontSize = 14.sp,
                    color = AppColors.TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        isLogoutDialogOpen = false
                        onLogout()
                    }
                ) {
                    Text(
                        text = strings.profileLogoutBtn,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE53935)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { isLogoutDialogOpen = false }) {
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

    // Delete Account Confirmation Dialog
    if (isDeleteAccountDialogOpen) {
        AlertDialog(
            onDismissRequest = { isDeleteAccountDialogOpen = false },
            title = {
                Text(
                    text = strings.profileDeleteAccountConfirmTitle,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
            },
            text = {
                Text(
                    text = strings.profileDeleteAccountConfirmMsg,
                    fontSize = 14.sp,
                    color = AppColors.TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        isDeleteAccountDialogOpen = false
                        onLogout()
                    }
                ) {
                    Text(
                        text = strings.profileDeleteAccountBtn,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE53935)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { isDeleteAccountDialogOpen = false }) {
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                // Screen Title
                Column {
                    Text(
                        text = strings.meTitle,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = strings.meSubtitle,
                        fontSize = 13.sp,
                        color = AppColors.TextSecondary
                    )
                }
            }

            // 1. Profile Section with 3D Block Avatar & Edit Badge
            item {
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { isEditProfileDialogOpen = true },
                    padding = 16.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 3D Block Avatar (Pure avatar without green circle)
                        Box(
                            modifier = Modifier.size(56.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            BlockAvatarView(
                                avatarType = userProfile.avatarType,
                                size = 54.dp
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = userProfile.nickname,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.TextPrimary
                                )

                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(AppColors.PrimaryLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Profile",
                                        tint = AppColors.PrimaryDark,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = userProfile.email,
                                fontSize = 12.sp,
                                color = AppColors.TextSecondary
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Edit",
                            tint = AppColors.TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 2. Equipment Management Section
            item {
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNavigateToEquipment,
                    padding = 16.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppColors.PrimaryLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = strings.meEquipmentManageTitle,
                                tint = AppColors.PrimaryDark,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.meEquipmentManageTitle,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = strings.meEquipmentManageSubtitle,
                                fontSize = 12.sp,
                                color = AppColors.TextSecondary
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Navigate",
                            tint = AppColors.TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 3. Language Settings Section
            item {
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    padding = 16.dp
                ) {
                    Text(
                        text = strings.meLanguageSetting,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = strings.meLanguageDesc,
                        fontSize = 12.sp,
                        color = AppColors.TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Korean Option
                        LanguageOptionCard(
                            language = AppLanguage.KO,
                            title = "한국어",
                            subtitle = "Korean",
                            isSelected = currentLang == AppLanguage.KO,
                            onClick = { onLanguageChange(AppLanguage.KO) },
                            modifier = Modifier.weight(1f)
                        )

                        // English Option
                        LanguageOptionCard(
                            language = AppLanguage.EN,
                            title = "English",
                            subtitle = "영어",
                            isSelected = currentLang == AppLanguage.EN,
                            onClick = { onLanguageChange(AppLanguage.EN) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 4. Tutorial Restart Section
            item {
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { isRestartTutorialDialogOpen = true },
                    padding = 16.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = strings.tutorialRestartBtn,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.PrimaryDark
                        )

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Restart",
                            tint = AppColors.PrimaryDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 5. Account Management (Logout & Delete Account)
            item {
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    padding = 14.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Logout Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pointerHoverIcon(PointerIcon.Hand)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = { isLogoutDialogOpen = true }
                                )
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = strings.profileLogoutBtn,
                                tint = AppColors.TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = strings.profileLogoutBtn,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.TextSecondary
                            )
                        }

                        // Delete Account Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pointerHoverIcon(PointerIcon.Hand)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = { isDeleteAccountDialogOpen = true }
                                )
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonRemove,
                                contentDescription = strings.profileDeleteAccountBtn,
                                tint = Color(0xFFE53935).copy(alpha = 0.8f),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = strings.profileDeleteAccountBtn,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFE53935).copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // 6. App Info
            item {
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    padding = 16.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = strings.appTitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )

                        Text(
                            text = strings.meAppVersion,
                            fontSize = 12.sp,
                            color = AppColors.TextMuted
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun LanguageOptionCard(
    language: AppLanguage,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val shape = RoundedCornerShape(14.dp)
    val bgColor = if (isSelected) AppColors.PrimaryLight.copy(alpha = 0.45f) else Color.Transparent
    val borderColor = if (isSelected) AppColors.Primary else AppColors.Border.copy(alpha = 0.6f)
    val borderWidth = if (isSelected) 1.5.dp else 0.5.dp

    Box(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .border(borderWidth, borderColor, shape)
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 12.dp, horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) AppColors.PrimaryDark else AppColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = if (isSelected) AppColors.Primary else AppColors.TextSecondary
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(AppColors.Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = strings.selected,
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}
