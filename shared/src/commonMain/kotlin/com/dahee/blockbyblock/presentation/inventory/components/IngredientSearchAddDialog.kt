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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.dahee.blockbyblock.domain.model.CatalogIngredient
import com.dahee.blockbyblock.domain.model.Ingredient
import com.dahee.blockbyblock.domain.model.IngredientCategory
import com.dahee.blockbyblock.domain.model.IngredientStatus

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IngredientSearchAddDialog(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: IngredientCategory?,
    onCategoryFilterChange: (IngredientCategory?) -> Unit,
    catalogResults: List<CatalogIngredient>,
    registeredIngredients: List<Ingredient>,
    onAddFromCatalog: (CatalogIngredient, IngredientStatus) -> Unit,
    onAddCustomIngredient: (String, IngredientStatus) -> Unit,
    onRemoveIngredient: (String) -> Unit = {},
    currentPage: Int = 1,
    totalPages: Int = 1,
    onPageChange: (Int) -> Unit = {},
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current
    val focusManager = LocalFocusManager.current

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
                    // Option to add custom ingredient if search query is entered (shown on page 1)
                    if (searchQuery.isNotBlank() && currentPage == 1) {
                        val trimmedQuery = searchQuery.trim()
                        val alreadyRegistered = registeredIngredients.find {
                            it.name.trim().equals(trimmedQuery, ignoreCase = true)
                        }
                        val isActiveRegistered = alreadyRegistered != null && alreadyRegistered.status != IngredientStatus.OUT_OF_STOCK

                        item {
                            val customCardBg = when (alreadyRegistered?.status) {
                                IngredientStatus.STOCK -> Color(0xBFDAFFD3)
                                IngredientStatus.CART -> Color(0xCDFCDCD1)
                                else -> AppColors.PrimaryLight.copy(alpha = 0.3f)
                            }
                            val customBorderColor = when (alreadyRegistered?.status) {
                                IngredientStatus.STOCK -> Color(0xBFDAFFD3)
                                IngredientStatus.CART -> Color(0xCDFCDCD1)
                                else -> AppColors.Primary.copy(alpha = 0.4f)
                            }

                            AppCard(
                                modifier = Modifier.fillMaxWidth(),
                                padding = 10.dp,
                                backgroundColor = customCardBg,
                                borderColor = customBorderColor
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
                                            color = if (isActiveRegistered) AppColors.TextPrimary else AppColors.PrimaryDark
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    if (isActiveRegistered) {
                                        val (badgeBg, badgeText, badgeColor) = when (alreadyRegistered.status) {
                                            IngredientStatus.STOCK -> Triple(Color.White.copy(alpha = 0.9f), strings.alreadyAddedInStock, AppColors.PrimaryDark)
                                            IngredientStatus.OUT_OF_STOCK -> Triple(AppColors.SurfaceVariant, strings.alreadyAddedConsumed, AppColors.TextMuted)
                                            IngredientStatus.CART -> Triple(Color.White.copy(alpha = 0.9f), strings.alreadyAddedCart, Color(0xFFC2410C))
                                        }
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Left: In Stock / Cart indicator (same size as add buttons: 68.dp x 30.dp)
                                            Box(
                                                modifier = Modifier
                                                    .width(68.dp)
                                                    .height(30.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(badgeBg),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = badgeColor,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Text(
                                                        text = badgeText,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = badgeColor
                                                    )
                                                }
                                            }

                                            // Right: Cancel button (same size as add buttons: 68.dp x 30.dp)
                                            Box(
                                                modifier = Modifier
                                                    .width(68.dp)
                                                    .height(30.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color.White.copy(alpha = 0.9f))
                                                    .border(1.dp, AppColors.Border, RoundedCornerShape(8.dp))
                                                    .pointerHoverIcon(PointerIcon.Hand)
                                                    .clickable(
                                                        interactionSource = remember { MutableInteractionSource() },
                                                        indication = null,
                                                        onClick = { onRemoveIngredient(alreadyRegistered.id) }
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = strings.cancel,
                                                        tint = AppColors.TextSecondary,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Text(
                                                        text = strings.cancel,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = AppColors.TextSecondary
                                                    )
                                                }
                                            }
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
                                                                IngredientStatus.STOCK
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
                                                                IngredientStatus.CART
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
                                onAddStock = {
                                    onAddFromCatalog(item, IngredientStatus.STOCK)
                                },
                                onAddCart = {
                                    onAddFromCatalog(item, IngredientStatus.CART)
                                },
                                onRemove = if (alreadyRegistered != null) {
                                    { onRemoveIngredient(alreadyRegistered.id) }
                                } else null
                            )
                        }
                    }
                }

                // 5. Catalog Pagination Controls
                if (totalPages > 1) {
                    CatalogPaginationControls(
                        currentPage = currentPage,
                        totalPages = totalPages,
                        onPageChange = onPageChange
                    )
                }
            }
        }
    }
}

@Composable
private fun CatalogPaginationControls(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit
) {
    val strings = LocalStrings.current
    val hasPrevious = currentPage > 1
    val hasNext = currentPage < totalPages

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Prev Page Button
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (hasPrevious) AppColors.SurfaceVariant else Color.Transparent)
                .border(
                    width = 0.5.dp,
                    color = if (hasPrevious) AppColors.Border else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                )
                .pointerHoverIcon(if (hasPrevious) PointerIcon.Hand else PointerIcon.Default)
                .clickable(
                    enabled = hasPrevious,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onPageChange(currentPage - 1) }
                )
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = strings.prevPageBtn,
                tint = if (hasPrevious) AppColors.TextPrimary else AppColors.TextMuted,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = strings.prevPageBtn,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (hasPrevious) AppColors.TextPrimary else AppColors.TextMuted
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Page Indicator
        Text(
            text = "$currentPage / $totalPages",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.TextSecondary
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Next Page Button
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (hasNext) AppColors.SurfaceVariant else Color.Transparent)
                .border(
                    width = 0.5.dp,
                    color = if (hasNext) AppColors.Border else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                )
                .pointerHoverIcon(if (hasNext) PointerIcon.Hand else PointerIcon.Default)
                .clickable(
                    enabled = hasNext,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onPageChange(currentPage + 1) }
                )
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = strings.nextPageBtn,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (hasNext) AppColors.TextPrimary else AppColors.TextMuted
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = strings.nextPageBtn,
                tint = if (hasNext) AppColors.TextPrimary else AppColors.TextMuted,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun CatalogIngredientRow(
    item: CatalogIngredient,
    existingIngredient: Ingredient?,
    onAddStock: () -> Unit,
    onAddCart: () -> Unit,
    onRemove: (() -> Unit)? = null
) {
    val strings = LocalStrings.current
    val isActiveRegistered = existingIngredient != null && existingIngredient.status != IngredientStatus.OUT_OF_STOCK

    val cardBg = when (existingIngredient?.status) {
        IngredientStatus.STOCK -> Color(0xBFDAFFD3)
        IngredientStatus.CART -> Color(0xCDFCDCD1)
        else -> AppColors.Surface
    }
    val cardBorder = when (existingIngredient?.status) {
        IngredientStatus.STOCK -> Color(0xBFDAFFD3)
        IngredientStatus.CART -> Color(0xCDFCDCD1)
        else -> AppColors.Border.copy(alpha = 0.6f)
    }

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        padding = 10.dp,
        backgroundColor = cardBg,
        borderColor = cardBorder
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
                    color = AppColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = strings.ingredientCategoryName(item.category),
                        fontSize = 11.sp,
                        color = if (isActiveRegistered) AppColors.TextSecondary else AppColors.TextMuted
                    )

                    // Small indicator if item was previously consumed / out of stock
                    if (existingIngredient?.status == IngredientStatus.OUT_OF_STOCK) {
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

            Spacer(modifier = Modifier.width(8.dp))

            // Right: Either Active Registered Badge (with cancel button) OR Two stacked Add Buttons (allows adding consumed items back!)
            if (isActiveRegistered) {
                val (badgeBg, badgeText, badgeColor) = when (existingIngredient.status) {
                    IngredientStatus.STOCK -> Triple(Color.White.copy(alpha = 0.9f), strings.alreadyAddedInStock, AppColors.PrimaryDark)
                    IngredientStatus.OUT_OF_STOCK -> Triple(AppColors.SurfaceVariant, strings.alreadyAddedConsumed, AppColors.TextMuted)
                    IngredientStatus.CART -> Triple(Color.White.copy(alpha = 0.9f), strings.alreadyAddedCart, Color(0xFFC2410C))
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: In Stock / Cart indicator (same size as add buttons: 68.dp x 30.dp)
                    Box(
                        modifier = Modifier
                            .width(68.dp)
                            .height(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = badgeColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = badgeText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor
                            )
                        }
                    }

                    // Right: Cancel button (same size as add buttons: 68.dp x 30.dp)
                    Box(
                        modifier = Modifier
                            .width(68.dp)
                            .height(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.9f))
                            .border(1.dp, AppColors.Border, RoundedCornerShape(8.dp))
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable(
                                enabled = onRemove != null,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onRemove?.invoke() }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = strings.cancel,
                                tint = AppColors.TextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = strings.cancel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextSecondary
                            )
                        }
                    }
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
                                onClick = onAddStock
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
                                onClick = onAddCart
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
