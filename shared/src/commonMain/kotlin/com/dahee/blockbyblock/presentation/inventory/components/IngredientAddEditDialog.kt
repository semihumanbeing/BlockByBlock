package com.dahee.blockbyblock.presentation.inventory.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.dahee.blockbyblock.core.i18n.LocalStrings
import com.dahee.blockbyblock.core.theme.AppColors
import com.dahee.blockbyblock.core.ui.AppButton
import com.dahee.blockbyblock.core.ui.AppChip
import com.dahee.blockbyblock.core.ui.AppTextField
import com.dahee.blockbyblock.core.ui.ButtonVariant
import com.dahee.blockbyblock.core.ui.EditableNumberStepper
import com.dahee.blockbyblock.domain.model.Ingredient
import com.dahee.blockbyblock.domain.model.IngredientCategory
import com.dahee.blockbyblock.domain.model.IngredientStatus
import com.dahee.blockbyblock.domain.model.IngredientUnit

// Toggle flag to hide quantity stepper UI per user preference
private const val SHOW_QUANTITY_UI = false

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IngredientAddEditDialog(
    ingredient: Ingredient?,
    onDismiss: () -> Unit,
    onSave: (Ingredient) -> Unit,
    onDelete: ((String) -> Unit)? = null
) {
    val strings = LocalStrings.current
    val focusManager = LocalFocusManager.current
    val isEditing = ingredient != null && ingredient.name.isNotBlank()

    var name by remember { mutableStateOf(ingredient?.name ?: "") }
    var quantity by remember { mutableStateOf(ingredient?.quantity ?: 1.0) }
    var unit by remember { mutableStateOf(ingredient?.unit ?: IngredientUnit.GRAM) }
    var status by remember { mutableStateOf(ingredient?.status ?: IngredientStatus.IN_STOCK) }
    var category by remember { mutableStateOf(ingredient?.category ?: IngredientCategory.OTHER) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 620.dp)
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
                        text = if (isEditing) strings.inventoryEditTitle else strings.inventoryAddTitle,
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

                // 1. Status Tabs (In Stock vs Shopping Cart)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IngredientStatus.entries.forEach { s ->
                        AppChip(
                            text = strings.ingredientStatusName(s),
                            selected = status == s,
                            onClick = { status = s },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 2. Name Field
                AppTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "예: 닭가슴살, 양파, 계란",
                    label = strings.inventoryNameLabel
                )

                // 3. Quantity & Unit Selection (Preserved in code, hidden per user preference)
                if (SHOW_QUANTITY_UI) {
                    Column {
                        Text(
                            text = strings.inventoryQuantityLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            EditableNumberStepper(
                                value = quantity.toInt().coerceAtLeast(1),
                                onValueChange = { quantity = it.toDouble() },
                                suffix = "",
                                step = if (unit == IngredientUnit.GRAM || unit == IngredientUnit.ML) 10 else 1,
                                minValue = 1,
                                buttonSize = 26.dp,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = strings.inventoryUnitLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Radio-button style unit options
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IngredientUnit.entries.forEach { u ->
                                val isSelected = unit == u
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) AppColors.PrimaryLight.copy(alpha = 0.5f) else AppColors.Surface)
                                        .border(
                                            width = if (isSelected) 1.5.dp else 0.5.dp,
                                            color = if (isSelected) AppColors.Primary else AppColors.Border,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .pointerHoverIcon(PointerIcon.Hand)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = { unit = u }
                                        )
                                        .padding(vertical = 10.dp, horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    // Radio indicator circle
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .border(
                                                width = if (isSelected) 5.dp else 1.dp,
                                                color = if (isSelected) AppColors.Primary else AppColors.Border,
                                                shape = CircleShape
                                            )
                                            .background(if (isSelected) AppColors.Primary else Color.Transparent)
                                    )

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Text(
                                        text = u.symbol,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) AppColors.PrimaryDark else AppColors.TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Category Selection
                Column {
                    Text(
                        text = strings.inventoryCategoryLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IngredientCategory.entries.forEach { cat ->
                            AppChip(
                                text = strings.ingredientCategoryName(cat),
                                selected = category == cat,
                                onClick = { category = cat },
                                horizontalPadding = 8.dp,
                                verticalPadding = 5.dp,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Buttons (Delete vs Save)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (ingredient != null && isEditing && onDelete != null) {
                        AppButton(
                            text = strings.delete,
                            variant = ButtonVariant.DANGER,
                            onClick = { onDelete(ingredient.id) },
                            modifier = Modifier.weight(1f),
                            height = 46.dp
                        )
                    }

                    AppButton(
                        text = strings.save,
                        variant = ButtonVariant.PRIMARY,
                        onClick = {
                            if (name.isNotBlank()) {
                                val saved = Ingredient(
                                    id = ingredient?.id ?: "ing_${kotlin.random.Random.nextInt(100000, 999999)}",
                                    name = name.trim(),
                                    quantity = quantity,
                                    unit = unit,
                                    status = status,
                                    category = category
                                )
                                onSave(saved)
                            }
                        },
                        modifier = Modifier.weight(2f),
                        height = 46.dp
                    )
                }
            }
        }
    }
}
