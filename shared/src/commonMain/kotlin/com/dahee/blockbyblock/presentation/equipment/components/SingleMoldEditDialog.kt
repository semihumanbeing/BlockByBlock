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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.text.style.TextAlign
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

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import com.dahee.blockbyblock.core.ui.EditableCapacityStepper
import com.dahee.blockbyblock.core.utils.MoldCapacityFormatter
import com.dahee.blockbyblock.domain.model.MoldCapacityUnit

@Composable
fun SingleMoldEditDialog(
    equipment: Equipment,
    onDismiss: () -> Unit,
    onSave: (Equipment) -> Unit,
    onDelete: (String) -> Unit,
    capacityUnit: MoldCapacityUnit = MoldCapacityUnit.ML
) {
    val strings = LocalStrings.current
    val focusManager = LocalFocusManager.current
    val preset = equipment.moldPreset ?: MoldGridPreset.CUSTOM
    var moldName by remember { mutableStateOf(equipment.name) }
    var capacityMl by remember { mutableStateOf(equipment.customCapacityMl ?: equipment.displayCapacity) }
    var cellCount by remember { mutableStateOf(equipment.cellCount) }
    var quantity by remember { mutableStateOf(equipment.quantity) }
    var colorHex by remember { mutableStateOf(equipment.moldColorHex) }

    val (presetChips, cellStep, minCellCount) = when (preset) {
        MoldGridPreset.ML_500 -> Triple(listOf(1, 2, 4), 1, 1)
        MoldGridPreset.ML_250 -> Triple(listOf(2, 4, 8), 2, 1)
        MoldGridPreset.ML_125 -> Triple(listOf(4, 6, 8), 2, 1)
        MoldGridPreset.ML_30 -> Triple(listOf(8, 16, 24), 4, 4)
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

                // Top Preview Card (Large visual + capacity label, slot summary & steppers)
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
                        // Left Section: Mold View + Capacity & Name Input (Vertically centered together)
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(68.dp)
                            ) {
                                MoldView(
                                    preset = preset,
                                    moldColor = AppColors.hexToColor(colorHex),
                                    cellCount = cellCount,
                                    size = 64.dp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (capacityMl > 0) {
                                        MoldCapacityFormatter.formatCapacity(capacityMl, capacityUnit)
                                    } else {
                                        MoldCapacityFormatter.formatPreset(preset, capacityUnit, strings.moldPresetLabel(MoldGridPreset.CUSTOM))
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.TextPrimary,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Name Input Field (Clean white background, enlarged & bold placeholder)
                            Box(
                                modifier = Modifier
                                    .offset(y = (-6).dp)
                                    .widthIn(min = 110.dp, max = 150.dp)
                                    .height(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(1.dp, AppColors.Border, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 9.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                BasicTextField(
                                    value = moldName,
                                    onValueChange = { moldName = it },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.TextPrimary
                                    ),
                                    cursorBrush = SolidColor(AppColors.Primary),
                                    decorationBox = { innerTextField ->
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            if (moldName.isEmpty()) {
                                                Text(
                                                    text = strings.moldPresetLabel(preset) + " " + strings.slotCount(cellCount),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = AppColors.TextMuted,
                                                    maxLines = 1
                                                )
                                            }
                                            innerTextField()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Row 1: [ Quantity Stepper (1개) ] & [ Total slots summary ]
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = strings.totalSlotsPortion(cellCount * quantity),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.TextSecondary
                                )

                                EditableNumberStepper(
                                    value = quantity,
                                    onValueChange = { quantity = it },
                                    suffix = strings.unitPiece,
                                    step = 1,
                                    minValue = 1
                                )
                            }

                            // Row 2: [ 1칸 용량 Stepper ]
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                EditableCapacityStepper(
                                    capacityMl = capacityMl,
                                    unit = capacityUnit,
                                    onCapacityChange = { capacityMl = it }
                                )
                            }
                        }
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
                            val capacityLabel = if (capacityMl > 0) {
                                MoldCapacityFormatter.formatCapacity(capacityMl, capacityUnit)
                            } else {
                                MoldCapacityFormatter.formatPreset(preset, capacityUnit, strings.moldPresetLabel(MoldGridPreset.CUSTOM))
                            }
                            val fallbackName = "${capacityLabel} ${cellCount}${strings.unitSlot}"
                            val updatedName = moldName.ifBlank { fallbackName }
                            val updatedEquipment = equipment.copy(
                                name = updatedName,
                                customCapacityMl = capacityMl,
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
