package com.dahee.blockbyblock.presentation.mealplan.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.dahee.blockbyblock.core.i18n.LocalStrings
import com.dahee.blockbyblock.core.theme.AppColors
import com.dahee.blockbyblock.core.ui.AppButton
import com.dahee.blockbyblock.core.ui.AppTextField
import com.dahee.blockbyblock.core.ui.ButtonVariant
import com.dahee.blockbyblock.domain.model.MealBlockItem
import com.dahee.blockbyblock.domain.model.MealType
import com.dahee.blockbyblock.presentation.block.components.LegoTopViewBlock
import com.dahee.blockbyblock.presentation.mealplan.AvailableBlockPiece

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.window.DialogProperties

private data class AvailableBlockGroup(
    val blockId: String,
    val blockName: String,
    val blockColorHex: String,
    val moldCapacityMl: Int,
    val moldCellCount: Int,
    val remainingPieces: List<AvailableBlockPiece>
)

@Composable
fun MealRecordDialog(
    mealType: MealType,
    dateLabel: String,
    selectedBlocks: List<MealBlockItem>,
    availableBlocks: List<AvailableBlockPiece>,
    memoInput: String,
    onMemoChange: (String) -> Unit,
    onMoveToTop: (AvailableBlockPiece) -> Unit,
    onMoveToBottom: (MealBlockItem) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current
    val focusManager = LocalFocusManager.current
    var searchQuery by remember { mutableStateOf("") }

    // Group available individual pieces by their block type (blockId)
    val blockGroups = remember(availableBlocks, searchQuery) {
        val filtered = if (searchQuery.isBlank()) {
            availableBlocks
        } else {
            availableBlocks.filter { it.blockName.contains(searchQuery, ignoreCase = true) }
        }
        filtered.groupBy { it.blockId }.map { (blockId, pieces) ->
            val first = pieces.first()
            AvailableBlockGroup(
                blockId = blockId,
                blockName = first.blockName,
                blockColorHex = first.blockColorHex,
                moldCapacityMl = first.moldCapacityMl,
                moldCellCount = first.moldCellCount,
                remainingPieces = pieces
            )
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .imePadding()
                .clip(RoundedCornerShape(20.dp))
                .background(AppColors.Background)
                .border(0.5.dp, AppColors.Border.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    focusManager.clearFocus()
                }
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. Dialog Header (Fixed Top)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = strings.mealRecordDialogTitle(strings.mealTypeName(mealType)),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                        Text(
                            text = dateLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.Primary
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = strings.cancel,
                        tint = AppColors.TextMuted,
                        modifier = Modifier
                            .size(22.dp)
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onDismiss
                            )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Middle Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 2. Top Selected Blocks Area (Pure Lego Blocks Stacked as in Reference)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.mealRecordEatingBlocksTitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                        if (selectedBlocks.isNotEmpty()) {
                            Text(
                                text = strings.mealRecordRemoveHint,
                                fontSize = 11.sp,
                                color = AppColors.TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (selectedBlocks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(116.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(AppColors.SurfaceVariant.copy(alpha = 0.7f))
                                .border(1.dp, AppColors.Border, RoundedCornerShape(14.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = strings.mealRecordEmptyEatingBlocksHint,
                                fontSize = 13.sp,
                                color = AppColors.TextMuted
                            )
                        }
                    } else {
                        // Horizontal stacked tray where 2x4 lego blocks attach side by side like the reference!
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(116.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(AppColors.SurfaceVariant.copy(alpha = 0.5f))
                                .border(1.dp, AppColors.Border, RoundedCornerShape(14.dp))
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            selectedBlocks.forEach { item ->
                                Box(
                                    modifier = Modifier
                                        .pointerHoverIcon(PointerIcon.Hand)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = { onMoveToBottom(item) }
                                        )
                                ) {
                                    // Top-view Lego Food Block from drawable resource matching mold capacity
                                    LegoTopViewBlock(
                                        colorHex = item.blockColorHex,
                                        moldCapacityMl = item.moldCapacityMl,
                                        height = 96.dp
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Bottom Available Blocks Pool (Grouped Grid by Food Block Type)
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.mealRecordInventoryBlocksTitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                        if (availableBlocks.isNotEmpty()) {
                            Text(
                                text = strings.mealRecordAddHint,
                                fontSize = 11.sp,
                                color = AppColors.Primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Realtime Search Field
                    AppTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = strings.mealRecordSearchPlaceholder,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (availableBlocks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppColors.SurfaceVariant)
                                .border(0.5.dp, AppColors.Border, RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = strings.mealRecordNoBlocksInStock,
                                fontSize = 13.sp,
                                color = AppColors.TextMuted
                            )
                        }
                    } else if (blockGroups.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppColors.SurfaceVariant)
                                .border(0.5.dp, AppColors.Border, RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = strings.mealRecordNoMatchingBlocks,
                                fontSize = 13.sp,
                                color = AppColors.TextMuted
                            )
                        }
                    } else {
                        // 3 items per row grid
                        val rows = blockGroups.chunked(3)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rows.forEach { rowGroups ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    rowGroups.forEach { group ->
                                        AvailableBlockGridCard(
                                            group = group,
                                            onAddPiece = {
                                                val piece = group.remainingPieces.firstOrNull()
                                                if (piece != null) {
                                                    onMoveToTop(piece)
                                                }
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    // Fill empty slots in the row to keep equal width
                                    repeat(3 - rowGroups.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Memo Input
                Column {
                    Text(
                        text = strings.mealRecordMemoLabel,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    AppTextField(
                        value = memoInput,
                        onValueChange = onMemoChange,
                        placeholder = strings.mealRecordMemoPlaceholder,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Fixed Bottom Action Buttons (Always visible above keyboard)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppButton(
                    text = strings.cancel,
                    variant = ButtonVariant.SECONDARY,
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )

                AppButton(
                    text = strings.save,
                    variant = ButtonVariant.PRIMARY,
                    enabled = selectedBlocks.isNotEmpty(),
                    onClick = onSave,
                    modifier = Modifier.weight(1.5f)
                )
            }
            }
        }
    }
}

@Composable
private fun AvailableBlockGridCard(
    group: AvailableBlockGroup,
    onAddPiece: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAvailable = group.remainingPieces.isNotEmpty()
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(AppColors.Surface)
            .border(
                width = if (isAvailable) 1.dp else 0.5.dp,
                color = if (isAvailable) AppColors.Border else AppColors.Border.copy(alpha = 0.4f),
                shape = shape
            )
            .then(
                if (isAvailable) {
                    Modifier
                        .pointerHoverIcon(PointerIcon.Hand)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onAddPiece
                        )
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 6.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 1. Lego Top View Food Block from drawable resource matching mold capacity
            LegoTopViewBlock(
                colorHex = group.blockColorHex,
                moldCapacityMl = group.moldCapacityMl,
                width = 30.dp,
                height = 54.dp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 2. Block Name
            Text(
                text = group.blockName,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isAvailable) AppColors.TextPrimary else AppColors.TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            // 3. Count Badge
            Text(
                text = "${group.remainingPieces.size}개",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isAvailable) AppColors.Primary else AppColors.TextMuted
            )
        }
    }
}
