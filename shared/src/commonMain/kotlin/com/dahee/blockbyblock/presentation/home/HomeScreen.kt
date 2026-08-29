package com.dahee.blockbyblock.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dahee.blockbyblock.core.i18n.LocalStrings
import com.dahee.blockbyblock.core.theme.AppColors
import com.dahee.blockbyblock.core.ui.AppCard
import com.dahee.blockbyblock.domain.model.CookingToolType
import com.dahee.blockbyblock.domain.model.MoldGridPreset
import com.dahee.blockbyblock.presentation.equipment.EquipmentViewModel
import com.dahee.blockbyblock.presentation.equipment.components.CookingToolVisual
import com.dahee.blockbyblock.presentation.equipment.components.SouperMoldBrickView

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: EquipmentViewModel,
    onNavigateToEquipment: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val strings = LocalStrings.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            HomeHeader()
        }

        // 1. Equipment & Mold Status Section (Dynamic SouperMoldBrickView + Cooking Tool visuals)
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.homeEquipmentStatusTitle,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )

                    Text(
                        text = strings.homeManageBtn,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.Primary,
                        modifier = Modifier
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onNavigateToEquipment
                            )
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (uiState.allEquipments.isEmpty()) {
                    AppCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onNavigateToEquipment,
                        padding = 20.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = strings.homeNoEquipmentRegistered,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = strings.homeRegisterEquipmentNow,
                                fontSize = 12.sp,
                                color = AppColors.Primary
                            )
                        }
                    }
                } else {
                    // Registered mold cards (horizontal scroll)
                    if (uiState.moldEquipments.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(uiState.moldEquipments, key = { it.id }) { mold ->
                                AppCard(
                                    modifier = Modifier.width(160.dp),
                                    onClick = onNavigateToEquipment,
                                    padding = 12.dp
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        SouperMoldBrickView(
                                            preset = mold.moldPreset ?: MoldGridPreset.CUSTOM,
                                            moldColor = AppColors.hexToColor(mold.moldColorHex),
                                            cellCount = mold.cellCount,
                                            size = 64.dp
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = "${mold.displayCapacity}ml (${strings.slotCount(mold.cellCount)})",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AppColors.TextPrimary
                                        )

                                        Text(
                                            text = strings.pieceCount(mold.quantity),
                                            fontSize = 11.sp,
                                            color = AppColors.TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Registered cooking tools (FlowRow)
                    if (uiState.toolEquipments.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            uiState.toolEquipments.forEach { tool ->
                                val toolDisplayName = tool.toolType?.let { strings.cookingToolName(it) } ?: tool.name

                                Box(
                                    modifier = Modifier
                                        .width(84.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(AppColors.Surface)
                                        .border(1.dp, AppColors.Border, RoundedCornerShape(12.dp))
                                        .padding(vertical = 10.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        tool.toolType?.let { toolType ->
                                            CookingToolVisual(
                                                type = toolType,
                                                size = 40.dp
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                        }
                                        Text(
                                            text = toolDisplayName,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = AppColors.TextPrimary,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Today's Meal Plan Section
        item {
            HomeSection(
                title = strings.homeTodayMealTitle,
                subtitle = strings.homeTodayMealSubtitle,
                iconVector = Icons.Default.DateRange
            )
        }

        // 3. Inventory Stock Section
        item {
            HomeSection(
                title = strings.homeInventoryTitle,
                subtitle = strings.homeInventorySubtitle,
                iconVector = Icons.Default.ShoppingCart
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun HomeHeader() {
    val strings = LocalStrings.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = strings.appTitle,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
            Text(
                text = strings.homeAppSubtitle,
                fontSize = 13.sp,
                color = AppColors.TextSecondary
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(AppColors.Surface)
                .border(0.5.dp, AppColors.Border.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = AppColors.TextPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun HomeSection(
    title: String,
    subtitle: String,
    iconVector: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = title,
                tint = AppColors.Primary,
                modifier = Modifier
                    .padding(end = 6.dp)
                    .size(20.dp)
            )
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        AppCard(
            modifier = Modifier.fillMaxWidth(),
            padding = 24.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = AppColors.TextMuted
                )
            }
        }
    }
}
