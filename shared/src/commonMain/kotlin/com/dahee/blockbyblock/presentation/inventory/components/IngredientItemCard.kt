package com.dahee.blockbyblock.presentation.inventory.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dahee.blockbyblock.core.i18n.LocalStrings
import com.dahee.blockbyblock.core.theme.AppColors
import com.dahee.blockbyblock.core.ui.AppCard
import com.dahee.blockbyblock.core.ui.EditableNumberStepper
import com.dahee.blockbyblock.domain.model.Ingredient
import com.dahee.blockbyblock.domain.model.IngredientStatus
import com.dahee.blockbyblock.domain.model.IngredientUnit

// Toggle flag to hide quantity stepper UI per user preference while preserving implementation
private const val SHOW_QUANTITY_UI = false

/**
 * Checklist item card supporting:
 * - IN_STOCK: Active inventory with 1-tap [Mark as Consumed] action
 * - CONSUMED: Dimmed item within In Stock tab with 1-tap [Move to Cart] or [Restore to Stock] action
 * - SHOPPING_CART: Purchase checklist item with 1-tap [Mark as In Stock] action
 */
@Composable
fun IngredientItemCard(
    ingredient: Ingredient,
    onToggleStatus: () -> Unit,
    onMarkAsConsumed: () -> Unit = {},
    onMoveToCart: () -> Unit = {},
    onRestoreToStock: () -> Unit = {},
    onQuantityChange: (Double) -> Unit = {},
    onUnitChange: (IngredientUnit) -> Unit = {},
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val isInStock = ingredient.status == IngredientStatus.IN_STOCK
    val isConsumed = ingredient.status == IngredientStatus.CONSUMED
    val isCart = ingredient.status == IngredientStatus.SHOPPING_CART

    // Step calculation: step by 1 for pieces/lbs, step by 10 for gram/ml
    val step = when (ingredient.unit) {
        IngredientUnit.PIECE, IngredientUnit.LBS -> 1
        IngredientUnit.GRAM, IngredientUnit.ML -> if (ingredient.quantity >= 100.0) 10 else 10
    }

    val checkboxBg by animateColorAsState(
        targetValue = when {
            isInStock -> AppColors.Primary
            isConsumed -> AppColors.SurfaceVariant
            else -> AppColors.Surface
        },
        animationSpec = tween(200)
    )
    val checkboxBorder by animateColorAsState(
        targetValue = when {
            isInStock -> AppColors.PrimaryDark
            isConsumed -> AppColors.Border
            else -> Color(0xFFEA580C).copy(alpha = 0.7f)
        },
        animationSpec = tween(200)
    )

    AppCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = when {
            isConsumed -> AppColors.SurfaceVariant
            isInStock -> AppColors.Surface
            else -> AppColors.Surface
        },
        borderColor = when {
            isConsumed -> AppColors.Border.copy(alpha = 0.6f)
            isInStock -> AppColors.Border
            else -> Color(0xFFEA580C).copy(alpha = 0.35f)
        },
        borderWidth = 0.5.dp,
        elevation = 0.dp,
        padding = 12.dp,
        onClick = onEdit
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Checklist style toggle circle
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .shadow(if (isInStock) 1.dp else 0.dp, CircleShape, spotColor = AppColors.Shadow)
                    .clip(CircleShape)
                    .background(checkboxBg)
                    .border(
                        width = if (isInStock) 1.5.dp else 1.5.dp,
                        color = checkboxBorder,
                        shape = CircleShape
                    )
                    .pointerHoverIcon(PointerIcon.Hand)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onToggleStatus
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isInStock) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = strings.selected,
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                } else if (isConsumed) {
                    // Small shopping cart hint icon indicating click to move to cart
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = strings.moveToCartBtn,
                        tint = Color(0xFFEA580C).copy(alpha = 0.75f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 2. Ingredient Name & Category
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = ingredient.name,
                        fontSize = 15.sp,
                        fontWeight = if (isInStock) FontWeight.Bold else FontWeight.Medium,
                        color = if (isConsumed) AppColors.TextMuted else AppColors.TextPrimary,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // Status Badge (In Stock vs Consumed vs Shopping Cart)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when {
                                    isInStock -> AppColors.PrimaryLight.copy(alpha = 0.7f)
                                    isConsumed -> AppColors.SurfaceVariant
                                    else -> AppColors.AccentLight
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = strings.ingredientStatusName(ingredient.status),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isInStock -> AppColors.PrimaryDark
                                isConsumed -> AppColors.TextMuted
                                else -> Color(0xFFC2410C)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Category Tag
                Text(
                    text = strings.ingredientCategoryName(ingredient.category),
                    fontSize = 11.sp,
                    color = AppColors.TextMuted
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 3. Quick Action Buttons per Status (Frictionless 1-Tap Management)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                when {
                    // IN_STOCK: Show Mark as Consumed action button
                    isInStock -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppColors.SurfaceVariant)
                                .border(0.5.dp, AppColors.Border, RoundedCornerShape(8.dp))
                                .pointerHoverIcon(PointerIcon.Hand)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onMarkAsConsumed
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = strings.markAsConsumedBtn,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextSecondary
                            )
                        }
                    }

                    // CONSUMED: Show Move to Cart and Restore to Stock action buttons
                    isConsumed -> {
                        // Restore to stock button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppColors.PrimaryLight.copy(alpha = 0.8f))
                                .pointerHoverIcon(PointerIcon.Hand)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onRestoreToStock
                                )
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = strings.restoreToStockBtn,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.PrimaryDark
                            )
                        }

                        // Move to cart button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEA580C))
                                .pointerHoverIcon(PointerIcon.Hand)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onMoveToCart
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = strings.moveToCartBtn,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // SHOPPING_CART: No action button on the right per user preference
                    isCart -> {}
                }
            }

            // 4. Quantity Stepper UI (Preserved in code, hidden per user request)
            if (SHOW_QUANTITY_UI) {
                Spacer(modifier = Modifier.width(8.dp))
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        EditableNumberStepper(
                            value = ingredient.quantity.toInt().coerceAtLeast(1),
                            onValueChange = { onQuantityChange(it.toDouble()) },
                            suffix = "",
                            step = step,
                            minValue = 1,
                            buttonSize = 20.dp
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IngredientUnit.entries.forEach { u ->
                                val isSelected = ingredient.unit == u
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) AppColors.Primary else AppColors.SurfaceVariant)
                                        .border(
                                            width = if (isSelected) 1.dp else 0.5.dp,
                                            color = if (isSelected) AppColors.PrimaryDark else AppColors.Border,
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                        .pointerHoverIcon(PointerIcon.Hand)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = { onUnitChange(u) }
                                        )
                                        .padding(horizontal = 5.dp, vertical = 3.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = u.symbol,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else AppColors.TextSecondary
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
