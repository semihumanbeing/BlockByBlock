package com.dahee.blockbyblock.presentation.mealplan.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.dahee.blockbyblock.domain.model.MealPreset
import com.dahee.blockbyblock.domain.model.MealType
import com.dahee.blockbyblock.presentation.block.components.FoodBlockTopView
import com.dahee.blockbyblock.presentation.mealplan.AvailableBlockPiece

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Add
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
    titleInput: String = "",
    onTitleChange: (String) -> Unit = {},
    memoInput: String,
    onMemoChange: (String) -> Unit,
    onMoveToTop: (AvailableBlockPiece) -> Unit,
    onMoveToBottom: (MealBlockItem) -> Unit,
    savedPresets: List<MealPreset> = emptyList(),
    onSaveCurrentAsPreset: (String) -> Unit = {},
    onApplyPreset: (MealPreset) -> Unit = {},
    onDeletePreset: (String) -> Unit = {},
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current
    val focusManager = LocalFocusManager.current
    var searchQuery by remember { mutableStateOf("") }
    var isSavePresetMode by remember { mutableStateOf(false) }
    var presetNameInput by remember { mutableStateOf("") }

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
                .widthIn(max = 520.dp)
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
                        val headerMealName = if (mealType == MealType.SNACK) {
                            titleInput.ifBlank { "추가" }
                        } else {
                            strings.mealTypeName(mealType)
                        }
                        Text(
                            text = strings.mealRecordDialogTitle(headerMealName),
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
                            .size(20.dp)
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
                // 1.5 Snack / Extra Meal Title Input (간식 식단 기록 시 식사 제목 입력창 - 최대 50자 제한)
                if (mealType == MealType.SNACK) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.mealTitleLabel,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextPrimary
                            )
                            Text(
                                text = "${titleInput.length}/50",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (titleInput.length >= 50) Color(0xFFE53935) else AppColors.TextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        AppTextField(
                            value = titleInput,
                            onValueChange = { if (it.length <= 50) onTitleChange(it) },
                            placeholder = strings.mealTitlePlaceholder,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                // 2. Bento Box Tray (Centered lunchbox container)
                BentoLunchBoxView(
                    blocks = selectedBlocks,
                    isDynamicExpandable = true,
                    blockHeight = 96.dp,
                    onBlockClick = onMoveToBottom
                )

                // 2.2 Save Current Combination as Preset Button & Inline Input
                if (selectedBlocks.isNotEmpty()) {
                    if (!isSavePresetMode) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppColors.PrimaryLight.copy(alpha = 0.5f))
                                .pointerHoverIcon(PointerIcon.Hand)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        presetNameInput = titleInput.ifBlank { "" }
                                        isSavePresetMode = true
                                    }
                                )
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = strings.saveMealPreset,
                                tint = AppColors.Primary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.saveMealPreset,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.Primary
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(AppColors.SurfaceVariant.copy(alpha = 0.6f))
                                .border(0.5.dp, AppColors.Border, RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = strings.enterPresetName,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextPrimary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                AppTextField(
                                    value = presetNameInput,
                                    onValueChange = { presetNameInput = it },
                                    placeholder = strings.presetNamePlaceholder,
                                    modifier = Modifier.weight(1f)
                                )

                                AppButton(
                                    text = strings.save,
                                    variant = ButtonVariant.PRIMARY,
                                    onClick = {
                                        onSaveCurrentAsPreset(presetNameInput)
                                        isSavePresetMode = false
                                        presetNameInput = ""
                                    }
                                )

                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = strings.cancel,
                                    tint = AppColors.TextMuted,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .pointerHoverIcon(PointerIcon.Hand)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = { isSavePresetMode = false }
                                        )
                                )
                            }
                        }
                    }
                }

                // 2.5 Saved Meal Presets Section (저장된 식단 세트 - 도시락통 안에 레고 블록들이 들어있는 모습)
                if (savedPresets.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = strings.savedMealPresets,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.TextPrimary
                                )
                                Text(
                                    text = "${savedPresets.size}개",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.Primary
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            savedPresets.forEach { preset ->
                                SavedMealPresetCard(
                                    preset = preset,
                                    onApply = { onApplyPreset(preset) },
                                    onDelete = { onDeletePreset(preset.id) }
                                )
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
            // 1. Food Block Top View from drawable resource matching mold capacity
            FoodBlockTopView(
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

@Composable
private fun SavedMealPresetCard(
    preset: MealPreset,
    onApply: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .width(180.dp)
            .clip(shape)
            .background(AppColors.Surface)
            .border(1.dp, AppColors.Border, shape)
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onApply
            )
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Preset Name & Delete X Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = preset.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = strings.deletePreset,
                    tint = AppColors.TextMuted,
                    modifier = Modifier
                        .size(16.dp)
                        .pointerHoverIcon(PointerIcon.Hand)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onDelete
                        )
                )
            }

            // Authentic Bento Lunch Box Preview with Lego blocks inside
            BentoLunchBoxView(
                blocks = preset.blocks,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            )

            // Bottom Apply Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${preset.blocks.size}개 블록",
                    fontSize = 10.5.sp,
                    color = AppColors.TextSecondary
                )

                Text(
                    text = strings.applyPreset,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Primary
                )
            }
        }
    }
}



