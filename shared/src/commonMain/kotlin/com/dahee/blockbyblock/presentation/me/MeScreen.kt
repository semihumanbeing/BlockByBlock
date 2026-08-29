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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dahee.blockbyblock.core.i18n.AppLanguage
import com.dahee.blockbyblock.core.i18n.LocalAppLanguage
import com.dahee.blockbyblock.core.i18n.LocalStrings
import com.dahee.blockbyblock.core.theme.AppColors
import com.dahee.blockbyblock.core.ui.AppCard

@Composable
fun MeScreen(
    onLanguageChange: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val currentLang = LocalAppLanguage.current

    var nickname by remember { mutableStateOf(strings.meProfileDefaultName) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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

            // 1. Profile Section
            item {
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    padding = 16.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar Icon
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .shadow(2.dp, CircleShape, spotColor = AppColors.Shadow)
                                .clip(CircleShape)
                                .background(AppColors.PrimaryLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = strings.meProfileSection,
                                tint = AppColors.PrimaryDark,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = nickname,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = strings.meProfileDesc,
                                fontSize = 12.sp,
                                color = AppColors.TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // 2. Language Settings Section
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

                    // Language Option Buttons (Korean / English)
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

            // 3. App Info
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
