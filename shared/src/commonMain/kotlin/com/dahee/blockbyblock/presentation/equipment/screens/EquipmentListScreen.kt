package com.dahee.blockbyblock.presentation.equipment.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dahee.blockbyblock.core.i18n.LocalStrings
import com.dahee.blockbyblock.core.theme.AppColors
import com.dahee.blockbyblock.core.ui.AppButton
import com.dahee.blockbyblock.core.ui.AppCard
import com.dahee.blockbyblock.core.ui.ButtonVariant
import com.dahee.blockbyblock.core.ui.EditableNumberStepper
import com.dahee.blockbyblock.domain.model.Equipment
import com.dahee.blockbyblock.domain.model.MoldGridPreset
import com.dahee.blockbyblock.presentation.equipment.EquipmentViewModel
import com.dahee.blockbyblock.presentation.equipment.components.CookingToolVisual
import com.dahee.blockbyblock.presentation.equipment.components.MoldView
import com.dahee.blockbyblock.presentation.equipment.components.SingleMoldEditDialog
import com.dahee.blockbyblock.presentation.equipment.state.EquipmentUiState

import androidx.compose.ui.platform.LocalFocusManager

import com.dahee.blockbyblock.core.utils.MoldCapacityFormatter
import com.dahee.blockbyblock.domain.model.MoldCapacityUnit
import com.dahee.blockbyblock.presentation.equipment.components.MoldUnitToggle

/**
 * Screen 3: Shows all registered molds & cooking equipment at a glance,
 * with top-right [Edit All] button or individual mold click to edit via dialog.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EquipmentListScreen(
    uiState: EquipmentUiState,
    viewModel: EquipmentViewModel,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val focusManager = LocalFocusManager.current

    // Single mold edit dialog modal
    if (uiState.editingMold != null) {
        SingleMoldEditDialog(
            equipment = uiState.editingMold,
            capacityUnit = uiState.capacityUnit,
            onDismiss = { viewModel.onCloseMoldEditDialog() },
            onSave = { updated -> viewModel.onSaveSingleMold(updated) },
            onDelete = { id -> viewModel.onDeleteSingleMold(id) }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
            }
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(14.dp))
            // Header: Title + [Edit All] button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = strings.listTitle,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                    Text(
                        text = strings.listSubtitle,
                        fontSize = 13.sp,
                        color = AppColors.TextSecondary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MoldUnitToggle(
                        currentUnit = uiState.capacityUnit,
                        onUnitChange = { viewModel.onToggleCapacityUnit(it) }
                    )

                    // Top-right [Edit All] button (Warm Honey Yellow from palette)
                    AppButton(
                        text = strings.editAllBtn,
                        variant = ButtonVariant.WARM_YELLOW,
                        onClick = { viewModel.onOpenEditScreen() },
                        height = 38.dp
                    )
                }
            }
        }

        // 1. Registered Molds List Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${strings.moldListSection} (${uiState.moldEquipments.size})",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
            }
        }

        if (uiState.moldEquipments.isEmpty()) {
            item {
                EmptySectionCard(
                    title = strings.noMoldsRegistered,
                    description = strings.emptyEquipmentHint
                )
            }
        } else {
            items(uiState.moldEquipments, key = { it.id }) { mold ->
                MoldItemCard(
                    equipment = mold,
                    capacityUnit = uiState.capacityUnit,
                    onClick = { viewModel.onOpenMoldEditDialog(mold) },
                    onQuantitySet = { qty -> viewModel.onSetEquipmentQuantity(mold.id, qty) }
                )
            }
        }

        // 2. Registered Cooking Tools Section
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${strings.cookingToolListSection} (${uiState.toolEquipments.size})",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
            }
        }

        if (uiState.toolEquipments.isEmpty()) {
            item {
                EmptySectionCard(
                    title = strings.noToolsRegistered,
                    description = strings.emptyEquipmentHint
                )
            }
        } else {
            item {
                val chunks = uiState.toolEquipments.chunked(4)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    chunks.forEach { rowTools ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowTools.forEach { tool ->
                                val toolDisplayName = tool.toolType?.let { strings.cookingToolName(it) } ?: tool.name
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    tool.toolType?.let { toolType ->
                                        CookingToolVisual(
                                            type = toolType,
                                            size = 64.dp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }
                                    Text(
                                        text = toolDisplayName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = AppColors.TextPrimary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        maxLines = 1
                                    )
                                }
                            }
                            // Fill empty columns if the row has fewer than 4 items
                            repeat(4 - rowTools.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Screen 3 Mold Item Card:
 * - Click to open edit dialog modal
 * - Dynamic MoldView + Capacity & Slot text + Stepper
 */
@Composable
private fun MoldItemCard(
    equipment: Equipment,
    capacityUnit: MoldCapacityUnit,
    onClick: () -> Unit,
    onQuantitySet: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val capacityLabel = if (equipment.customCapacityMl != null) {
        MoldCapacityFormatter.formatCapacity(equipment.customCapacityMl, capacityUnit)
    } else if (equipment.moldPreset != null && equipment.moldPreset != MoldGridPreset.CUSTOM) {
        MoldCapacityFormatter.formatPreset(equipment.moldPreset, capacityUnit, strings.moldPresetLabel(MoldGridPreset.CUSTOM))
    } else {
        MoldCapacityFormatter.formatCapacity(equipment.displayCapacity, capacityUnit)
    }

    val defaultNamePattern = "${equipment.displayCapacity}ml ${equipment.cellCount}칸"
    val hasCustomName = equipment.name.isNotBlank() && equipment.name != defaultNamePattern && !equipment.name.startsWith(capacityLabel)

    AppCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = Color.White,
        borderColor = AppColors.Border.copy(alpha = 0.6f),
        borderWidth = 0.5.dp,
        elevation = 1.dp,
        padding = 16.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mold silicone brick visual (reflecting dynamic color in real-time)
            MoldView(
                preset = equipment.moldPreset ?: MoldGridPreset.CUSTOM,
                moldColor = AppColors.hexToColor(equipment.moldColorHex),
                cellCount = equipment.cellCount,
                size = 60.dp
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Capacity & Slots or Custom Name
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                if (hasCustomName) {
                    Text(
                        text = equipment.name,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = capacityLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.TextSecondary
                        )
                        Text(
                            text = "·",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextMuted
                        )
                        Text(
                            text = strings.slotCount(equipment.cellCount),
                            fontSize = 13.sp,
                            color = AppColors.TextSecondary
                        )
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = capacityLabel,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )

                        Text(
                            text = "·",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextMuted
                        )

                        Text(
                            text = strings.slotCount(equipment.cellCount),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Quantity Stepper ([-] [N개] [+])
            EditableNumberStepper(
                value = equipment.quantity,
                onValueChange = onQuantitySet,
                suffix = strings.unitPiece,
                step = 1,
                minValue = 1
            )

            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
private fun EmptySectionCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        padding = 24.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = AppColors.TextMuted
            )
        }
    }
}
