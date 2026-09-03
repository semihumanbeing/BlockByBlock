package com.dahee.blockbyblock.presentation.inventory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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
import com.dahee.blockbyblock.core.ui.AppCard
import com.dahee.blockbyblock.core.ui.AppChip
import com.dahee.blockbyblock.core.ui.AppTextField
import com.dahee.blockbyblock.core.ui.EditableNumberStepper
import com.dahee.blockbyblock.domain.model.CatalogIngredient
import com.dahee.blockbyblock.domain.model.Ingredient
import com.dahee.blockbyblock.domain.model.IngredientCategory
import com.dahee.blockbyblock.domain.model.IngredientStatus
import com.dahee.blockbyblock.domain.model.IngredientUnit

// Toggle flag to hide quantity stepper UI per user preference
private const val SHOW_QUANTITY_UI = false

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IngredientSearchAddDialog(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: IngredientCategory?,
    onCategoryFilterChange: (IngredientCategory?) -> Unit,
    catalogResults: List<CatalogIngredient>,
    registeredIngredients: List<Ingredient>,
    onAddFromCatalog: (CatalogIngredient, Double, IngredientUnit, IngredientStatus) -> Unit,
    onAddCustomIngredient: (String, Double, IngredientUnit, IngredientStatus) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current
    val focusManager = LocalFocusManager.current

    var customQuantity by remember { mutableStateOf(100.0) }
    var customUnit by remember { mutableStateOf(IngredientUnit.GRAM) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 680.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(AppColors.Background)
                .border(0.5.dp, AppColors.Border.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    focusManager.clearFocus()
                }
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.catalogSearchTitle,
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

                // 2. Search Bar
                AppTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = strings.catalogSearchPlaceholder,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = AppColors.TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // 3. Category Filter Chips (Horizontal Scroll)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        AppChip(
                            text = strings.inventoryTabAll,
                            selected = selectedCategory == null,
                            onClick = { onCategoryFilterChange(null) },
                            horizontalPadding = 8.dp,
                            verticalPadding = 4.dp,
                            fontSize = 11.sp
                        )
                    }

                    items(IngredientCategory.entries) { cat ->
                        AppChip(
                            text = strings.ingredientCategoryName(cat),
                            selected = selectedCategory == cat,
                            onClick = { onCategoryFilterChange(cat) },
                            horizontalPadding = 8.dp,
                            verticalPadding = 4.dp,
                            fontSize = 11.sp
                        )
                    }
                }

                // 4. Catalog Search Results or Custom Add Option
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Option to add custom ingredient if search query is entered
                    if (searchQuery.isNotBlank()) {
                        val trimmedQuery = searchQuery.trim()
                        val alreadyRegistered = registeredIngredients.find {
                            it.name.trim().equals(trimmedQuery, ignoreCase = true)
                        }
                        val isActiveRegistered = alreadyRegistered != null && alreadyRegistered.status != IngredientStatus.CONSUMED

                        item {
                            AppCard(
                                modifier = Modifier.fillMaxWidth(),
                                padding = 10.dp,
                                backgroundColor = if (isActiveRegistered) AppColors.SurfaceVariant else AppColors.PrimaryLight.copy(alpha = 0.3f),
                                borderColor = if (isActiveRegistered) AppColors.Border else AppColors.Primary.copy(alpha = 0.4f)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = strings.catalogAddCustomBtn(trimmedQuery),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isActiveRegistered) AppColors.TextSecondary else AppColors.PrimaryDark
                                        )

                                        if (SHOW_QUANTITY_UI && !isActiveRegistered) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                val step = if (customUnit == IngredientUnit.GRAM || customUnit == IngredientUnit.ML) 10 else 1
                                                EditableNumberStepper(
                                                    value = customQuantity.toInt().coerceAtLeast(1),
                                                    onValueChange = { customQuantity = it.toDouble() },
                                                    suffix = "",
                                                    step = step,
                                                    minValue = 1,
                                                    buttonSize = 18.dp
                                                )

                                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    IngredientUnit.entries.forEach { u ->
                                                        val isSelected = customUnit == u
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
                                                                    onClick = {
                                                                        customUnit = u
                                                                        if ((u == IngredientUnit.GRAM || u == IngredientUnit.ML) && customQuantity < 10.0) {
                                                                            customQuantity = 100.0
                                                                        } else if ((u == IngredientUnit.PIECE || u == IngredientUnit.LBS) && customQuantity >= 100.0) {
                                                                            customQuantity = 1.0
                                                                        }
                                                                    }
                                                                )
                                                                .padding(horizontal = 4.dp, vertical = 2.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = u.symbol,
                                                                fontSize = 10.sp,
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                                color = if (isSelected) Color.White else AppColors.TextSecondary
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    if (isActiveRegistered) {
                                        val (badgeBg, badgeText, badgeColor) = when (alreadyRegistered.status) {
                                            IngredientStatus.IN_STOCK -> Triple(AppColors.PrimaryLight, strings.alreadyAddedInStock, AppColors.PrimaryDark)
                                            IngredientStatus.CONSUMED -> Triple(AppColors.SurfaceVariant, strings.alreadyAddedConsumed, AppColors.TextMuted)
                                            IngredientStatus.SHOPPING_CART -> Triple(AppColors.AccentLight, strings.alreadyAddedCart, Color(0xFFC2410C))
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(badgeBg)
                                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = badgeColor,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = badgeText,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = badgeColor
                                            )
                                        }
                                    } else {
                                        // Side-by-side equal-sized add buttons: Left = In Stock, Right = Shopping Cart
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .width(68.dp)
                                                    .height(30.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(AppColors.Primary)
                                                    .pointerHoverIcon(PointerIcon.Hand)
                                                    .clickable(
                                                        interactionSource = remember { MutableInteractionSource() },
                                                        indication = null,
                                                        onClick = {
                                                            onAddCustomIngredient(
                                                                trimmedQuery,
                                                                customQuantity,
                                                                customUnit,
                                                                IngredientStatus.IN_STOCK
                                                            )
                                                        }
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = strings.addInStockBtn,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .width(68.dp)
                                                    .height(30.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFFEA580C))
                                                    .pointerHoverIcon(PointerIcon.Hand)
                                                    .clickable(
                                                        interactionSource = remember { MutableInteractionSource() },
                                                        indication = null,
                                                        onClick = {
                                                            onAddCustomIngredient(
                                                                trimmedQuery,
                                                                customQuantity,
                                                                customUnit,
                                                                IngredientStatus.SHOPPING_CART
                                                            )
                                                        }
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = strings.addCartBtn,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (catalogResults.isEmpty() && searchQuery.isBlank()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = strings.catalogNoResults,
                                    fontSize = 13.sp,
                                    color = AppColors.TextMuted
                                )
                            }
                        }
                    } else {
                        items(catalogResults, key = { it.id }) { item ->
                            val alreadyRegistered = registeredIngredients.find {
                                it.name.trim().equals(item.name.trim(), ignoreCase = true)
                            }
                            CatalogIngredientRow(
                                item = item,
                                existingIngredient = alreadyRegistered,
                                onAddStock = { qty, unit ->
                                    onAddFromCatalog(item, qty, unit, IngredientStatus.IN_STOCK)
                                },
                                onAddCart = { qty, unit ->
                                    onAddFromCatalog(item, qty, unit, IngredientStatus.SHOPPING_CART)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogIngredientRow(
    item: CatalogIngredient,
    existingIngredient: Ingredient?,
    onAddStock: (Double, IngredientUnit) -> Unit,
    onAddCart: (Double, IngredientUnit) -> Unit
) {
    val strings = LocalStrings.current
    var quantity by remember(item) { mutableStateOf(item.defaultQuantity) }
    var unit by remember(item) { mutableStateOf(item.defaultUnit) }

    val step = if (unit == IngredientUnit.GRAM || unit == IngredientUnit.ML) 10 else 1
    val isActiveRegistered = existingIngredient != null && existingIngredient.status != IngredientStatus.CONSUMED

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        padding = 10.dp,
        backgroundColor = if (isActiveRegistered) AppColors.SurfaceVariant.copy(alpha = 0.5f) else AppColors.Surface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Name & Category (with Consumed indicator if applicable)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActiveRegistered) AppColors.TextSecondary else AppColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = strings.ingredientCategoryName(item.category),
                        fontSize = 11.sp,
                        color = AppColors.TextMuted
                    )

                    // Small indicator if item was previously consumed
                    if (existingIngredient?.status == IngredientStatus.CONSUMED) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(AppColors.SurfaceVariant)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = strings.alreadyAddedConsumed,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextMuted
                            )
                        }
                    }
                }
            }

            // Middle: Quantity Stepper & Radio Unit Selector (Preserved in code, hidden per user request)
            if (SHOW_QUANTITY_UI && !isActiveRegistered) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    EditableNumberStepper(
                        value = quantity.toInt().coerceAtLeast(1),
                        onValueChange = { quantity = it.toDouble() },
                        suffix = "",
                        step = step,
                        minValue = 1,
                        buttonSize = 18.dp
                    )

                    // Radio-style unit chips
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        IngredientUnit.entries.forEach { u ->
                            val isSelected = unit == u
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
                                        onClick = {
                                            unit = u
                                            if ((u == IngredientUnit.GRAM || u == IngredientUnit.ML) && quantity < 10.0) {
                                                quantity = 100.0
                                            } else if ((u == IngredientUnit.PIECE || u == IngredientUnit.LBS) && quantity >= 100.0) {
                                                quantity = 1.0
                                            }
                                        }
                                    )
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = u.symbol,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else AppColors.TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right: Either Active Registered Badge OR Two stacked Add Buttons (allows adding consumed items back!)
            if (isActiveRegistered) {
                val (badgeBg, badgeText, badgeColor) = when (existingIngredient.status) {
                    IngredientStatus.IN_STOCK -> Triple(AppColors.PrimaryLight, strings.alreadyAddedInStock, AppColors.PrimaryDark)
                    IngredientStatus.CONSUMED -> Triple(AppColors.SurfaceVariant, strings.alreadyAddedConsumed, AppColors.TextMuted)
                    IngredientStatus.SHOPPING_CART -> Triple(AppColors.AccentLight, strings.alreadyAddedCart, Color(0xFFC2410C))
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            } else {
                // Side-by-side equal-sized add buttons: Left = In Stock, Right = Shopping Cart
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: In Stock button
                    Box(
                        modifier = Modifier
                            .width(68.dp)
                            .height(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AppColors.Primary)
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onAddStock(quantity, unit) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = strings.addInStockBtn,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Right: Shopping Cart button
                    Box(
                        modifier = Modifier
                            .width(68.dp)
                            .height(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEA580C))
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onAddCart(quantity, unit) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = strings.addCartBtn,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
