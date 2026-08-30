package com.dahee.blockbyblock.presentation.equipment.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.dahee.blockbyblock.core.theme.AppColors
import com.dahee.blockbyblock.core.ui.AppButton
import com.dahee.blockbyblock.core.ui.AppChip
import com.dahee.blockbyblock.core.ui.AppTextField
import com.dahee.blockbyblock.core.ui.ButtonVariant
import com.dahee.blockbyblock.core.ui.MoldGridVisualizer
import com.dahee.blockbyblock.domain.model.CookingToolType
import com.dahee.blockbyblock.domain.model.Equipment
import com.dahee.blockbyblock.domain.model.EquipmentCategory
import com.dahee.blockbyblock.domain.model.MoldGridPreset

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EquipmentAddEditDialog(
    equipment: Equipment?,
    onDismiss: () -> Unit,
    onSave: (Equipment) -> Unit,
    onDelete: ((String) -> Unit)? = null
) {
    val isEditing = equipment != null

    var category by remember { mutableStateOf(equipment?.category ?: EquipmentCategory.MOLD) }
    var name by remember { mutableStateOf(equipment?.name ?: "") }
    var quantity by remember { mutableStateOf(equipment?.quantity ?: 1) }
    
    // Mold state
    var selectedPreset by remember { mutableStateOf(equipment?.moldPreset ?: MoldGridPreset.ML_250) }
    var customCapacityText by remember { mutableStateOf(equipment?.customCapacityMl?.toString() ?: "200") }
    var cellCount by remember { mutableStateOf(equipment?.cellCount ?: 6) }
    var selectedColorHex by remember { mutableStateOf(equipment?.moldColorHex ?: "#A7F3D0") }

    // Tool state
    var selectedToolType by remember { mutableStateOf(equipment?.toolType ?: CookingToolType.GAS_STOVE) }
    var memo by remember { mutableStateOf(equipment?.memo ?: "") }

    // Live capacity & grid calculations
    val currentCapacity = if (selectedPreset == MoldGridPreset.CUSTOM) {
        customCapacityText.toIntOrNull() ?: 0
    } else {
        selectedPreset.capacityMl
    }

    val currentRows = if (selectedPreset == MoldGridPreset.CUSTOM) 2 else selectedPreset.rows
    val currentCols = if (selectedPreset == MoldGridPreset.CUSTOM) 2 else selectedPreset.cols

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 680.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(AppColors.Surface)
                .border(1.dp, AppColors.Border, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) "Edit Equipment" else "Register Equipment",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )

                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
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

                Spacer(modifier = Modifier.height(16.dp))

                // Category selector tabs (Mold vs Cooking Tool)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EquipmentCategory.entries.forEach { cat ->
                        AppChip(
                            text = cat.displayName,
                            selected = category == cat,
                            onClick = {
                                if (category != cat) {
                                    category = cat
                                    if (!isEditing) {
                                        name = ""
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (category == EquipmentCategory.MOLD) {
                    // [Mold Configuration Section]
                    
                    // 1. Grid preset selector
                    Text(
                        text = "Mold Preset",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MoldGridPreset.entries.forEach { preset ->
                            val label = if (preset == MoldGridPreset.CUSTOM) {
                                "Custom"
                            } else {
                                preset.label
                            }
                            AppChip(
                                text = label,
                                selected = selectedPreset == preset,
                                onClick = {
                                    selectedPreset = preset
                                    if (name.isBlank() || name.contains("Mold") || name.contains("몰드")) {
                                        name = if (preset != MoldGridPreset.CUSTOM) {
                                            "${preset.label} ${cellCount}-slot Mold"
                                        } else {
                                            "Custom ${cellCount}-slot Mold"
                                        }
                                    }
                                }
                            )
                        }
                    }

                    if (selectedPreset == MoldGridPreset.CUSTOM) {
                        Spacer(modifier = Modifier.height(10.dp))
                        AppTextField(
                            value = customCapacityText,
                            onValueChange = { customCapacityText = it },
                            placeholder = "Enter capacity (e.g. 300)",
                            label = "Custom Capacity (ml)",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Mold slot count
                    Text(
                        text = "Slots per Mold",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val cellOptions = listOf(1, 2, 4, 6, 8, 12)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        cellOptions.forEach { count ->
                            AppChip(
                                text = "$count",
                                selected = cellCount == count,
                                onClick = {
                                    cellCount = count
                                    if (name.isBlank() || name.contains("Mold") || name.contains("몰드")) {
                                        name = "${selectedPreset.label} ${count}-slot Mold"
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. Quantity Stepper
                    Text(
                        text = "Quantity",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(AppColors.SurfaceVariant)
                                .border(1.dp, AppColors.Border, RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { if (quantity > 1) quantity-- },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("▼", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (quantity > 1) AppColors.TextPrimary else AppColors.TextMuted)
                            }

                            Text(
                                text = "$quantity",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextPrimary,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { quantity++ },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("▲", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppColors.Primary)
                            }
                        }

                        Text(
                            text = "Total ${cellCount * quantity} slots available",
                            fontSize = 12.sp,
                            color = AppColors.TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4. Silicone pastel mold color
                    Text(
                        text = "Mold Color (Silicone)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppColors.MoldColors.forEach { color ->
                            val hex = AppColors.colorToHex(color)
                            val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) AppColors.Primary else AppColors.Border,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColorHex = hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = AppColors.TextPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Live mold preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppColors.SurfaceVariant)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            MoldGridVisualizer(
                                rows = currentRows,
                                cols = currentCols,
                                moldColor = AppColors.hexToColor(selectedColorHex),
                                cellSize = 13.dp,
                                cellSpacing = 2.5.dp
                            )

                            Column {
                                Text(
                                    text = "Preview: ${selectedPreset.label} (${currentRows}x${currentCols})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.TextPrimary
                                )
                                Text(
                                    text = "${cellCount}-slot mold x $quantity (${currentCapacity}ml per slot)",
                                    fontSize = 12.sp,
                                    color = AppColors.TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mold name field
                    AppTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = "e.g. 250ml 6-slot Meal Mold",
                        label = "Mold Name"
                    )

                } else {
                    // [Cooking Tool Configuration Section]
                    Text(
                        text = "Tool Type",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CookingToolType.entries.forEach { tool ->
                            AppChip(
                                text = tool.displayName,
                                selected = selectedToolType == tool,
                                onClick = {
                                    selectedToolType = tool
                                    if (name.isBlank()) {
                                        name = tool.displayName
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quantity
                    Text(
                        text = "Quantity",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppColors.SurfaceVariant)
                            .border(1.dp, AppColors.Border, RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { if (quantity > 1) quantity-- },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("▼", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (quantity > 1) AppColors.TextPrimary else AppColors.TextMuted)
                        }

                        Text(
                            text = "$quantity",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { quantity++ },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("▲", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppColors.Primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    AppTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = "e.g. High-speed Blender",
                        label = "Tool Name"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    AppTextField(
                        value = memo,
                        onValueChange = { memo = it },
                        placeholder = "e.g. For sauces & smoothies",
                        label = "Memo"
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom Action Buttons (Save / Delete / Cancel)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (equipment != null && onDelete != null) {
                        AppButton(
                            text = "Delete",
                            variant = ButtonVariant.DANGER,
                            onClick = { onDelete(equipment.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    AppButton(
                        text = "Cancel",
                        variant = ButtonVariant.SECONDARY,
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )

                    AppButton(
                        text = if (isEditing) "Save Changes" else "Save",
                        variant = ButtonVariant.PRIMARY,
                        onClick = {
                            val finalName = if (name.isNotBlank()) name else {
                                if (category == EquipmentCategory.MOLD) "${selectedPreset.label} ${cellCount}-slot Mold" else selectedToolType.displayName
                            }
                            val savedEquipment = Equipment(
                                id = equipment?.id ?: "eq_${kotlin.random.Random.nextInt(100000, 999999)}",
                                name = finalName,
                                category = category,
                                moldPreset = if (category == EquipmentCategory.MOLD) selectedPreset else null,
                                customCapacityMl = if (category == EquipmentCategory.MOLD && selectedPreset == MoldGridPreset.CUSTOM) customCapacityText.toIntOrNull() else null,
                                cellCount = if (category == EquipmentCategory.MOLD) cellCount else 1,
                                moldColorHex = if (category == EquipmentCategory.MOLD) selectedColorHex else "#CBD5E1",
                                quantity = quantity,
                                toolType = if (category == EquipmentCategory.COOKING_TOOL) selectedToolType else null,
                                memo = if (category == EquipmentCategory.COOKING_TOOL) memo else "",
                                isPreset = equipment?.isPreset ?: false,
                                isOwned = true,
                                createdAt = equipment?.createdAt ?: 0L
                            )
                            onSave(savedEquipment)
                        },
                        modifier = Modifier.weight(1.2f)
                    )
                }
            }
        }
    }
}
