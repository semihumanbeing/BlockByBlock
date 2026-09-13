package com.dahee.blockbyblock.presentation.navigation

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import blockbyblock.shared.generated.resources.Res
import blockbyblock.shared.generated.resources.app_logo_emblem
import org.jetbrains.compose.resources.painterResource
import com.dahee.blockbyblock.core.i18n.LocalStrings
import com.dahee.blockbyblock.core.theme.AppColors
import com.dahee.blockbyblock.domain.model.UserProfile
import com.dahee.blockbyblock.presentation.me.components.BlockAvatarView

@Composable
fun AppSideNav(
    currentTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier,
    userProfile: UserProfile? = null,
    onLogoClick: () -> Unit = { onTabSelected(NavTab.MEAL_PLAN) }
) {
    val strings = LocalStrings.current
    val navWidth by animateDpAsState(
        targetValue = if (isExpanded) 220.dp else 72.dp,
        animationSpec = tween(durationMillis = 200)
    )

    Box(
        modifier = modifier
            .width(navWidth)
            .fillMaxHeight()
            .background(AppColors.Surface)
            .border(
                width = 1.dp,
                color = AppColors.Border
            )
            .padding(vertical = 12.dp, horizontal = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Logo & Hamburger Toggle Row
            if (isExpanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onLogoClick
                            )
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.app_logo_emblem),
                            contentDescription = "BlockByBlock Logo",
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Block",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D3B35)
                            )
                            Text(
                                text = "By",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF4B740)
                            )
                            Text(
                                text = "Block",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D3B35)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onToggleExpand
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuOpen,
                            contentDescription = "Collapse Menu",
                            tint = AppColors.TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .pointerHoverIcon(PointerIcon.Hand)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onToggleExpand
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(Res.drawable.app_logo_emblem),
                        contentDescription = "Expand Menu",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // 2. User Profile Info (1st row directly below logo - display only)
            if (userProfile != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.SurfaceVariant)
                        .padding(horizontal = if (isExpanded) 10.dp else 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = if (isExpanded) Arrangement.Start else Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier.size(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        BlockAvatarView(
                            avatarType = userProfile.avatarType,
                            size = 36.dp,
                            contentDescription = userProfile.nickname
                        )
                    }

                    if (isExpanded) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = userProfile.nickname,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Navigation Tab Items
            NavTab.entries.forEach { tab ->
                val isSelected = currentTab == tab
                val itemBgColor = if (isSelected) AppColors.PrimaryLight else Color.Transparent
                val itemContentColor = if (isSelected) AppColors.Primary else AppColors.TextSecondary
                val tabLabel = tab.getLabel(strings)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(itemBgColor)
                        .pointerHoverIcon(PointerIcon.Hand)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onTabSelected(tab) }
                        )
                        .padding(horizontal = if (isExpanded) 12.dp else 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = if (isExpanded) Arrangement.Start else Arrangement.Center
                ) {
                    Icon(
                        imageVector = tab.iconVector,
                        contentDescription = tabLabel,
                        tint = itemContentColor,
                        modifier = Modifier.size(24.dp)
                    )

                    if (isExpanded) {
                        Spacer(modifier = Modifier.width(14.dp))

                        Text(
                            text = tabLabel,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = itemContentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}
