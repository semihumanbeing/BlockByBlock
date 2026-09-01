package com.dahee.blockbyblock.presentation.equipment.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.dahee.blockbyblock.core.i18n.LocalStrings
import com.dahee.blockbyblock.core.theme.AppColors
import com.dahee.blockbyblock.core.ui.AppButton
import com.dahee.blockbyblock.core.ui.AppChip
import com.dahee.blockbyblock.core.ui.ButtonVariant
import com.dahee.blockbyblock.core.ui.CustomSlotChip
import com.dahee.blockbyblock.core.ui.EditableNumberStepper
import com.dahee.blockbyblock.domain.model.Equipment
import com.dahee.blockbyblock.domain.model.MoldGridPreset

import androidx.compose.ui.platform.LocalFocusManager

/**
 * Modal dialog to quickly edit/delete a single mold from Screen 3 list
 */
@Composable
fun SingleMoldEditDialog(
    equipment: Equipment,
    onDismiss: () -> Unit,
    onSave: (Equipment) -> Unit,
    onDelete: (String) -> Unit
) {
    val strings = LocalStrings.current
    val focusManager = LocalFocusManager.current
    var cellCount by remember { mutableStateOf(equipment.cellCount) }
    var quantity by remember { mutableStateOf(equipment.quantity) }
    var colorHex by remember { mutableStateOf(equipment.moldColorHex) }

    val preset = equipment.moldPreset ?: MoldGridPreset.CUSTOM
    val (presetChips, cellStep, minCellCount) = when (preset) {
        MoldGridPreset.ML_500 -> Triple(listOf(1, 2, 4), 1, 1)
        MoldGridPreset.ML_250 -> Triple(listOf(2, 4, 8), 2, 1)
        MoldGridPreset.ML_125 -> Triple(listOf(4, 6, 8), 2, 1)
        MoldGridPreset.ML_75 -> Triple(listOf(8, 16, 24), 4, 4)
        MoldGridPreset.CUSTOM -> Triple(listOf(6, 8, 16), 2, 1)
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
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
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.editMoldDialogTitle,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )

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

                // Top Preview Card (Large visual + capacity label, slot summary & quantity stepper)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Transparent)
                        .border(0.5.dp, AppColors.Border.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(76.dp)
                        ) {
                            MoldView(
                                preset = preset,
                                moldColor = AppColors.hexToColor(colorHex),
                                cellCount = cellCount,
                                size = 72.dp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${equipment.displayCapacity}ml",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = strings.totalSlotsPortion(cellCount * quantity),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.TextSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = strings.slotCount(cellCount),
                                fontSize = 12.sp,
                                color = AppColors.TextMuted
                            )
                        }

                        // Quantity Stepper
                        EditableNumberStepper(
                            value = quantity,
                            onValueChange = { quantity = it },
                            suffix = strings.unitPiece,
                            step = 1,
                            minValue = 1
                        )
                    }
                }

                // 1. Slot count selection (Right-aligned: preset chips + CustomSlotChip)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    presetChips.forEach { count ->
                        AppChip(
                            text = strings.slotCount(count),
                            selected = cellCount == count,
                            onClick = { cellCount = count },
                            horizontalPadding = 7.dp,
                            verticalPadding = 4.dp,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }

                    CustomSlotChip(
                        currentCellCount = cellCount,
                        presetList = presetChips,
                        onCellCountChange = { cellCount = it },
                        minCellCount = minCellCount,
                        horizontalPadding = 7.dp,
                        verticalPadding = 4.dp
                    )
                }

                // 2. Mold color palette (Right-aligned dots without label)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppColors.MoldColors.forEach { color ->
                        val hex = AppColors.colorToHex(color)
                        val isColorSelected = colorHex.equals(hex, ignoreCase = true)

                        Box(
                            modifier = Modifier
                                .padding(start = 6.dp)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isColorSelected) 2.5.dp else 1.dp,
                                    color = if (isColorSelected) AppColors.Primary else AppColors.Border,
                                    shape = CircleShape
                                )
                                .pointerHoverIcon(PointerIcon.Hand)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { colorHex = hex }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isColorSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = strings.selected,
                                    tint = AppColors.TextPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom actions: Delete vs Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AppButton(
                        text = strings.editMoldDialogDeleteBtn,
                        variant = ButtonVariant.DANGER,
                        onClick = { onDelete(equipment.id) },
                        modifier = Modifier.weight(1f),
                        height = 46.dp
                    )

                    AppButton(
                        text = strings.editMoldDialogSaveBtn,
                        variant = ButtonVariant.PRIMARY,
                        onClick = {
                            val updatedName = if (preset == MoldGridPreset.CUSTOM) {
                                "${equipment.displayCapacity}ml ${cellCount}${strings.unitSlot}"
                            } else {
                                "${strings.moldPresetLabel(preset)} ${cellCount}${strings.unitSlot}"
                            }
                            val updatedEquipment = equipment.copy(
                                name = updatedName,
                                cellCount = cellCount,
                                quantity = quantity,
                                moldColorHex = colorHex
                            )
                            onSave(updatedEquipment)
                        },
                        modifier = Modifier.weight(2f),
                        height = 46.dp
                    )
                }
            }
        }
    }
}
