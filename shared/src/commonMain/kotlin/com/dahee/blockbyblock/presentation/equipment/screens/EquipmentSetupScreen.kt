package com.dahee.blockbyblock.presentation.equipment.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dahee.blockbyblock.core.i18n.LocalStrings
import com.dahee.blockbyblock.core.theme.AppColors
import com.dahee.blockbyblock.core.ui.AppButton
import com.dahee.blockbyblock.core.ui.AppCard
import com.dahee.blockbyblock.core.ui.AppChip
import com.dahee.blockbyblock.core.ui.AppTextField
import com.dahee.blockbyblock.core.ui.ButtonVariant
import com.dahee.blockbyblock.core.ui.CustomSlotChip
import com.dahee.blockbyblock.core.ui.EditableNumberStepper
import com.dahee.blockbyblock.domain.model.CookingToolType
import com.dahee.blockbyblock.domain.model.MoldGridPreset
import com.dahee.blockbyblock.presentation.equipment.EquipmentViewModel
import com.dahee.blockbyblock.presentation.equipment.components.CookingToolVisual
import com.dahee.blockbyblock.presentation.equipment.components.MoldView
import com.dahee.blockbyblock.presentation.equipment.state.EquipmentUiState
import com.dahee.blockbyblock.presentation.equipment.state.MoldDraftConfig

/**
 * Screen 2: Mold configuration per size + quick cooking tool selector + bottom [Save] button
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EquipmentSetupScreen(
    uiState: EquipmentUiState,
    viewModel: EquipmentViewModel,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val focusManager = LocalFocusManager.current

    Column(
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
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                // Top Title & Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.setupTitle,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = strings.setupSubtitle,
                            fontSize = 13.sp,
                            color = AppColors.TextSecondary
                        )
                    }

                    if (uiState.allEquipments.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = strings.cancel,
                            tint = AppColors.TextMuted,
                            modifier = Modifier
                                .size(24.dp)
                                .pointerHoverIcon(PointerIcon.Hand)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { viewModel.onCancelSetup() }
                                )
                        )
                    }
                }
            }

            // Error Message
            if (uiState.errorMessage != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppColors.Danger.copy(alpha = 0.12f))
                            .border(1.dp, AppColors.Danger.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = strings.errorMinMoldRequired,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.Danger
                        )
                    }
                }
            }

            // 1. Silicone Mold Configuration Section
            item {
                Text(
                    text = strings.moldSectionTitle,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = strings.moldSectionSubtitle,
                    fontSize = 12.sp,
                    color = AppColors.TextSecondary
                )
            }

            items(uiState.moldDrafts, key = { it.preset.name }) { draft ->
                MoldSetupRow(
                    draft = draft,
                    onToggleSelect = { viewModel.onToggleMoldSelection(draft.preset) },
                    onQuantityChange = { delta -> viewModel.onUpdateMoldDraftQuantity(draft.preset, delta) },
                    onDirectQuantityChange = { qty ->
                        val delta = qty - draft.quantity
                        viewModel.onUpdateMoldDraftQuantity(draft.preset, delta)
                    },
                    onCellCountChange = { cellCount -> viewModel.onUpdateMoldDraftCellCount(draft.preset, cellCount) },
                    onColorChange = { colorHex -> viewModel.onUpdateMoldDraftColor(draft.preset, colorHex) },
                    onCustomCapacityChange = { cap -> viewModel.onUpdateMoldDraftCustomCapacity(cap) }
                )
            }

            // 2. Cooking Tool Selection Section
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = strings.toolsSectionTitle,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = strings.toolsSectionSubtitle,
                    fontSize = 12.sp,
                    color = AppColors.TextSecondary
                )
            }

            // Cooking Tools Grid: Pure visual icon + label beneath, centered
            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CookingToolType.entries.forEach { tool ->
                        val isSelected = uiState.selectedCookingTools.contains(tool)
                        val toolName = strings.cookingToolName(tool)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .width(78.dp)
                                .pointerHoverIcon(PointerIcon.Hand)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { viewModel.onToggleCookingTool(tool) }
                                )
                        ) {
                            Box(
                                contentAlignment = Alignment.TopEnd
                            ) {
                                CookingToolVisual(
                                    type = tool,
                                    size = 56.dp,
                                    modifier = Modifier.graphicsLayer {
                                        alpha = if (isSelected) 1.0f else 0.42f
                                        scaleX = if (isSelected) 1.05f else 0.95f
                                        scaleY = if (isSelected) 1.05f else 0.95f
                                    }
                                )

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(AppColors.Primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = strings.selected,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = toolName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) AppColors.PrimaryDark else AppColors.TextSecondary,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Bottom [Save] Button Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.Background)
                .border(0.5.dp, AppColors.Border.copy(alpha = 0.5f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .padding(16.dp)
        ) {
            AppButton(
                text = strings.setupSaveBtn,
                onClick = { viewModel.onSaveAllEquipment() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            )
        }
    }
}

/**
 * Mold Configuration Row:
 * - Tap anywhere on card to toggle selection
 * - [Left]: Large mold visual (68dp) + capacity label beneath
 * - [Right]: Unselected shows [+ Add] center button; Selected shows stepper / slot chips / color palette
 */
@Composable
private fun MoldSetupRow(
    draft: MoldDraftConfig,
    onToggleSelect: () -> Unit,
    onQuantityChange: (Int) -> Unit,
    onDirectQuantityChange: (Int) -> Unit,
    onCellCountChange: (Int) -> Unit,
    onColorChange: (String) -> Unit,
    onCustomCapacityChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val borderColor = if (draft.isSelected) AppColors.Primary else AppColors.Border.copy(alpha = 0.4f)
    val borderWidth = if (draft.isSelected) 1.dp else 0.5.dp
    val elevation = if (draft.isSelected) 2.dp else 0.dp
    val bgColor = if (draft.isSelected) AppColors.PrimaryLight.copy(alpha = 0.4f) else Color.Transparent

    val (presetChips, cellStep, minCellCount) = when (draft.preset) {
        MoldGridPreset.ML_500 -> Triple(listOf(1, 2, 4), 1, 1)
        MoldGridPreset.ML_250 -> Triple(listOf(2, 4, 8), 2, 1)
        MoldGridPreset.ML_125 -> Triple(listOf(4, 8, 12), 2, 1)
        MoldGridPreset.ML_75 -> Triple(listOf(8, 16, 24), 4, 4)
        MoldGridPreset.CUSTOM -> Triple(listOf(8, 16, 24), 4, 4)
    }

    AppCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = borderColor,
        borderWidth = borderWidth,
        elevation = elevation,
        cornerRadius = 18.dp,
        backgroundColor = bgColor,
        padding = 12.dp,
        onClick = onToggleSelect
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // [Left] Large Mold Visual + Capacity Label underneath (dimmed when unselected)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(70.dp)
                    .graphicsLayer {
                        alpha = if (draft.isSelected) 1.0f else 0.40f
                    }
                    .pointerHoverIcon(PointerIcon.Hand)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onToggleSelect
                    )
            ) {
                MoldView(
                    preset = draft.preset,
                    moldColor = AppColors.hexToColor(draft.moldColorHex),
                    cellCount = draft.cellCount,
                    size = 68.dp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (draft.preset == MoldGridPreset.CUSTOM && draft.isSelected) {
                        "${draft.customCapacityMl}ml"
                    } else {
                        strings.moldPresetLabel(draft.preset)
                    },
                    fontSize = 12.sp,
                    fontWeight = if (draft.isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (draft.isSelected) AppColors.TextPrimary else AppColors.TextMuted,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // [Right Area] Unselected: Center [+ Add] button; Selected: Stepper / Slot / Color controls
            if (!draft.isSelected) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(68.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .shadow(2.dp, RoundedCornerShape(12.dp), spotColor = AppColors.Shadow)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppColors.Primary)
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onToggleSelect
                            )
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = strings.add,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.addBtn,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. Top row: Quantity Stepper [▲ 1 piece ▼]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        EditableNumberStepper(
                            value = draft.quantity,
                            onValueChange = onDirectQuantityChange,
                            suffix = strings.unitPiece,
                            step = 1,
                            minValue = 1
                        )
                    }

                    // Custom capacity field when preset is CUSTOM
                    if (draft.preset == MoldGridPreset.CUSTOM) {
                        AppTextField(
                            value = if (draft.customCapacityMl == 0) "" else draft.customCapacityMl.toString(),
                            onValueChange = { onCustomCapacityChange(it.toIntOrNull() ?: 0) },
                            placeholder = strings.customCapacityPlaceholder,
                            label = strings.customCapacityLabel,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    // 2. Slot count selector (Right-aligned single row: Presets + CustomSlotChip)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        presetChips.forEach { count ->
                            AppChip(
                                text = strings.slotCount(count),
                                selected = draft.cellCount == count,
                                onClick = { onCellCountChange(count) },
                                horizontalPadding = 7.dp,
                                verticalPadding = 4.dp,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                        }

                        CustomSlotChip(
                            currentCellCount = draft.cellCount,
                            presetList = presetChips,
                            onCellCountChange = onCellCountChange,
                            minCellCount = minCellCount,
                            horizontalPadding = 7.dp,
                            verticalPadding = 4.dp
                        )
                    }

                    // 3. Mold color selector (Right-aligned dots without label)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppColors.MoldColors.forEach { color ->
                            val hex = AppColors.colorToHex(color)
                            val isColorSelected = draft.moldColorHex.equals(hex, ignoreCase = true)

                            Box(
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isColorSelected) 2.dp else 1.dp,
                                        color = if (isColorSelected) AppColors.Primary else AppColors.Border,
                                        shape = CircleShape
                                    )
                                    .pointerHoverIcon(PointerIcon.Hand)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { onColorChange(hex) }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isColorSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = strings.selected,
                                        tint = AppColors.TextPrimary,
                                        modifier = Modifier.size(12.dp)
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
