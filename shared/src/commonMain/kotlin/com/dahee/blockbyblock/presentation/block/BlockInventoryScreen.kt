package com.dahee.blockbyblock.presentation.block

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import blockbyblock.shared.generated.resources.Res
import blockbyblock.shared.generated.resources.empty_block_box
import org.jetbrains.compose.resources.painterResource
import com.dahee.blockbyblock.core.i18n.LocalStrings
import com.dahee.blockbyblock.core.theme.AppColors
import com.dahee.blockbyblock.core.ui.AppButton
import com.dahee.blockbyblock.core.ui.AppCard
import com.dahee.blockbyblock.core.ui.AppChip
import com.dahee.blockbyblock.core.ui.ButtonVariant
import com.dahee.blockbyblock.domain.model.FoodBlock
import com.dahee.blockbyblock.presentation.block.components.CreateBlockScreen
import com.dahee.blockbyblock.presentation.block.components.FoodBlock3DView

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BlockInventoryScreen(
    viewModel: BlockViewModel,
    onNavigateToInventory: () -> Unit = {},
    onNavigateToEquipment: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val strings = LocalStrings.current
    val focusManager = LocalFocusManager.current

    if (uiState.isCreateScreenOpen) {
        CreateBlockScreen(
            uiState = uiState,
            viewModel = viewModel,
            onNavigateToInventory = onNavigateToInventory,
            onNavigateToEquipment = onNavigateToEquipment,
            modifier = modifier
        )
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
            }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                // Top Header: Title & [+ Create Block] Action Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.blockTitle,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = strings.blockSubtitle,
                            fontSize = 13.sp,
                            color = AppColors.TextSecondary
                        )
                    }

                    // [+ Create Block] Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .shadow(2.dp, RoundedCornerShape(10.dp), spotColor = AppColors.Shadow)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppColors.Primary)
                            .border(1.dp, AppColors.PrimaryDark, RoundedCornerShape(10.dp))
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { viewModel.onOpenCreateScreen() }
                            )
                            .padding(horizontal = 12.dp, vertical = 9.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = strings.blockCreateBtn,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = strings.blockCreateBtn,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Tab Filters: [All] & Dynamic Mold Capacities (e.g. 500ml, 250ml, 125ml)
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // All Tab
                    item {
                        AppChip(
                            text = "${strings.inventoryTabAll} (${uiState.blocks.size})",
                            selected = uiState.selectedCapacityMl == null,
                            onClick = { viewModel.onCapacityFilterChange(null) }
                        )
                    }

                    // Mold Capacity Tabs
                    items(uiState.distinctMoldCapacities) { capacity ->
                        val count = uiState.blocks.count { it.moldCapacityMl == capacity }
                        val isSelected = uiState.selectedCapacityMl == capacity
                        AppChip(
                            text = "${capacity}ml ($count)",
                            selected = isSelected,
                            onClick = {
                                viewModel.onCapacityFilterChange(if (isSelected) null else capacity)
                            }
                        )
                    }
                }
            }

            // Stored Block List or Empty State
            if (uiState.filteredBlocks.isEmpty()) {
                item {
                    AppCard(
                        modifier = Modifier.fillMaxWidth(),
                        padding = 28.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(Res.drawable.empty_block_box),
                                contentDescription = strings.blockEmptyTitle,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(80.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = strings.blockEmptyTitle,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextSecondary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = strings.blockEmptyDesc,
                                fontSize = 12.sp,
                                color = AppColors.TextMuted,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            AppButton(
                                text = strings.blockCreateBtn,
                                variant = ButtonVariant.ACCENT,
                                onClick = { viewModel.onOpenCreateScreen() },
                                height = 36.dp
                            )
                        }
                    }
                }
            } else {
                items(uiState.filteredBlocks, key = { it.id }) { block ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                viewModel.onDeleteBlock(block.id)
                                true
                            } else {
                                false
                            }
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        enableDismissFromEndToStart = true,
                        backgroundContent = {
                            val isSwiping = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                if (isSwiping) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(0.5f)
                                            .clip(RoundedCornerShape(topEnd = 14.dp, bottomEnd = 14.dp))
                                            .background(Color(0xFFE53935))
                                            .padding(end = 20.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = strings.delete,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = strings.delete,
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    ) {
                        FoodBlockCard(
                            block = block,
                            onClick = { viewModel.onOpenEditScreen(block) },
                            onDelete = { viewModel.onDeleteBlock(block.id) }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FoodBlockCard(
    block: FoodBlock,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val isExpiringSoon = block.shelfLifeDays <= 7

    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        backgroundColor = if (isExpiringSoon) Color(0xFFFFF8F7) else AppColors.Surface,
        borderColor = if (isExpiringSoon) Color(0xFFEF5350).copy(alpha = 0.5f) else AppColors.Border,
        padding = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: 3D Food Block miniature matching mold capacity
            FoodBlock3DView(
                colorHex = block.blockColorHex,
                moldCapacityMl = block.moldCapacityMl,
                size = 52.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Center: Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AppColors.PrimaryLight)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${block.moldCapacityMl}ml (${strings.slotCount(block.moldCellCount)})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.PrimaryDark
                        )
                    }

                    if (isExpiringSoon) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFFEBEE))
                                .border(0.8.dp, Color(0xFFEF5350), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (block.shelfLifeDays <= 0) strings.shelfLifeExpired else strings.shelfLifeExpiringSoon(block.shelfLifeDays),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F)
                            )
                        }
                    } else {
                        Text(
                            text = strings.shelfLifeDays(block.shelfLifeDays),
                            fontSize = 11.sp,
                            color = AppColors.TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = block.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )

                if (block.mainIngredients.isNotEmpty() || block.subIngredients.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        block.mainIngredients.forEach { ing ->
                            Text(
                                text = "#$ing",
                                fontSize = 11.sp,
                                color = AppColors.PrimaryDark,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        block.subIngredients.forEach { sub ->
                            Text(
                                text = "#$sub",
                                fontSize = 11.sp,
                                color = AppColors.TextMuted
                            )
                        }
                    }
                }

                val cookingInfo = remember(block.cookingToolTypes, block.cookingToolType, block.cookingTemperature, block.cookingTimeMinutes, block.cookingTimeSeconds) {
                    val tools = block.effectiveCookingToolTypes
                    if (tools.isEmpty()) null
                    else {
                        val toolNames = tools.joinToString(", ") { strings.cookingToolName(it) }
                        val tempStr = block.cookingTemperature?.let { "${it}°C" }
                        val timeStr = buildString {
                            block.cookingTimeMinutes?.takeIf { it > 0 }?.let { append("${it}${strings.timeUnitMinutes}") }
                            block.cookingTimeSeconds?.takeIf { it > 0 }?.let {
                                if (isNotEmpty()) append(" ")
                                append("${it}${strings.timeUnitSeconds}")
                            }
                        }.ifBlank { null }
                        listOfNotNull(toolNames, tempStr, timeStr).joinToString(" · ")
                    }
                }

                if (cookingInfo != null) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "🍳 $cookingInfo",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Right-Center: Quantity Text (Bold & Large, centered vertically to the left of X)
            Text(
                text = strings.pieceCount(block.quantity),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AppColors.Primary
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Far-Right: X button for direct delete
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = strings.delete,
                tint = AppColors.TextMuted,
                modifier = Modifier
                    .size(20.dp)
                    .pointerHoverIcon(PointerIcon.Hand)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDelete
                    )
            )

            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}
