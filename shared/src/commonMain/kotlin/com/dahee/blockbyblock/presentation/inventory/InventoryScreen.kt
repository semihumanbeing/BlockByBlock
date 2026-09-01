package com.dahee.blockbyblock.presentation.inventory

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import blockbyblock.shared.generated.resources.Res
import blockbyblock.shared.generated.resources.other_utensils
import blockbyblock.shared.generated.resources.shopping_cart
import org.jetbrains.compose.resources.painterResource
import com.dahee.blockbyblock.getPlatform
import com.dahee.blockbyblock.core.i18n.LocalStrings
import com.dahee.blockbyblock.core.theme.AppColors
import com.dahee.blockbyblock.core.ui.AppButton
import com.dahee.blockbyblock.core.ui.AppCard
import com.dahee.blockbyblock.core.ui.AppChip
import com.dahee.blockbyblock.core.ui.ButtonVariant
import com.dahee.blockbyblock.domain.model.Ingredient
import com.dahee.blockbyblock.domain.model.IngredientCategory
import com.dahee.blockbyblock.domain.model.IngredientStatus
import com.dahee.blockbyblock.presentation.inventory.components.IngredientAddEditDialog
import com.dahee.blockbyblock.presentation.inventory.components.IngredientItemCard
import com.dahee.blockbyblock.presentation.inventory.components.IngredientSearchAddDialog
import com.dahee.blockbyblock.presentation.inventory.state.IngredientTab

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InventoryScreen(
    viewModel: IngredientViewModel,
    onCookClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val strings = LocalStrings.current
    val focusManager = LocalFocusManager.current
    val platform = remember { getPlatform() }
    val isWeb = platform.isWeb

    // 1. Master Catalog Search & Add Dialog
    if (uiState.isSearchCatalogDialogOpen) {
        IngredientSearchAddDialog(
            searchQuery = uiState.catalogSearchQuery,
            onSearchQueryChange = { viewModel.onCatalogSearchQueryChange(it) },
            selectedCategory = uiState.catalogCategoryFilter,
            onCategoryFilterChange = { viewModel.onCatalogCategoryFilterChange(it) },
            catalogResults = uiState.catalogResults,
            registeredIngredients = uiState.registeredIngredients,
            onAddFromCatalog = { item, qty, unit, status ->
                viewModel.onAddFromCatalog(item, qty, unit, status)
            },
            onAddCustomIngredient = { name, qty, unit, status ->
                viewModel.onAddCustomFromCatalogQuery(name, qty, unit, status)
            },
            onDismiss = { viewModel.onCloseSearchCatalogDialog() }
        )
    }

    // 2. Edit Modal Dialog
    if (uiState.isAddDialogOpen || uiState.editingIngredient != null) {
        IngredientAddEditDialog(
            ingredient = uiState.editingIngredient,
            onDismiss = { viewModel.onCloseDialog() },
            onSave = { saved -> viewModel.onSaveIngredient(saved) },
            onDelete = { id -> viewModel.onDeleteIngredient(id) }
        )
    }

    Box(
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
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                // Top Header: Title & [Search Ingredients] Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.inventoryTitle,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = strings.inventorySubtitle,
                            fontSize = 13.sp,
                            color = AppColors.TextSecondary
                        )
                    }

                    // Top-right Action Buttons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. [Search Ingredients] Button (Warm Terracotta Accent Color)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .shadow(2.dp, RoundedCornerShape(10.dp), spotColor = AppColors.Shadow)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AppColors.Accent)
                                .border(1.dp, Color(0xFFC05621), RoundedCornerShape(10.dp))
                                .pointerHoverIcon(PointerIcon.Hand)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { viewModel.onOpenSearchCatalogDialog() }
                                )
                                .padding(horizontal = 12.dp, vertical = 9.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = strings.inventorySearchBtn,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = strings.inventorySearchBtn,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // 2. [Cook Now] Button for Web (Identical size and height, placed next to search button)
                        if (isWeb) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .shadow(2.dp, RoundedCornerShape(10.dp), spotColor = AppColors.Shadow)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AppColors.Primary)
                                    .border(1.dp, AppColors.PrimaryDark, RoundedCornerShape(10.dp))
                                    .pointerHoverIcon(PointerIcon.Hand)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = onCookClick
                                    )
                                    .padding(horizontal = 12.dp, vertical = 9.dp)
                            ) {
                                Image(
                                    painter = painterResource(Res.drawable.other_utensils),
                                    contentDescription = strings.cookBtn,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = strings.cookBtn,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // 1. Tab Selector: [All] [In Stock] [Shopping Cart]
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // All Tab
                    AppChip(
                        text = "${strings.inventoryTabAll} (${uiState.totalCount})",
                        selected = uiState.selectedTab == IngredientTab.ALL,
                        onClick = { viewModel.onTabChange(IngredientTab.ALL) },
                        modifier = Modifier.weight(1f)
                    )

                    // In Stock Tab (Shows only in-stock count)
                    AppChip(
                        text = "${strings.inventoryTabInStock} (${uiState.inStockCount})",
                        selected = uiState.selectedTab == IngredientTab.IN_STOCK,
                        onClick = { viewModel.onTabChange(IngredientTab.IN_STOCK) },
                        modifier = Modifier.weight(1f)
                    )

                    // Shopping Cart Tab
                    AppChip(
                        text = "${strings.inventoryTabShoppingCart} (${uiState.shoppingCartCount})",
                        selected = uiState.selectedTab == IngredientTab.SHOPPING_CART,
                        onClick = { viewModel.onTabChange(IngredientTab.SHOPPING_CART) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 2. Category Filter Chips (Horizontal scroll)
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        AppChip(
                            text = strings.categoryAll,
                            selected = uiState.selectedCategory == null,
                            onClick = { viewModel.onCategoryFilterChange(null) },
                            horizontalPadding = 10.dp,
                            verticalPadding = 5.dp,
                            fontSize = 12.sp
                        )
                    }

                    items(IngredientCategory.entries) { cat ->
                        AppChip(
                            text = strings.ingredientCategoryName(cat),
                            selected = uiState.selectedCategory == cat,
                            onClick = { viewModel.onCategoryFilterChange(cat) },
                            horizontalPadding = 10.dp,
                            verticalPadding = 5.dp,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // 3. Checklist Ingredients List
            val isPantrySeparationActive = (uiState.selectedTab == IngredientTab.IN_STOCK || uiState.selectedTab == IngredientTab.ALL) && uiState.selectedCategory == null
            val freshIngredients = if (isPantrySeparationActive) {
                uiState.displayedIngredients.filter { it.category != IngredientCategory.SAUCE_SEASONING }
            } else {
                uiState.displayedIngredients
            }
            val seasoningIngredients = if (isPantrySeparationActive) {
                uiState.displayedIngredients.filter { it.category == IngredientCategory.SAUCE_SEASONING }
            } else {
                emptyList()
            }

            if (freshIngredients.isEmpty() && seasoningIngredients.isEmpty()) {
                item {
                    AppCard(
                        modifier = Modifier.fillMaxWidth(),
                        padding = 28.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(Res.drawable.shopping_cart),
                                contentDescription = strings.inventoryEmptyTitle,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (uiState.selectedTab == IngredientTab.SHOPPING_CART) {
                                    strings.cartEmptyTitle
                                } else {
                                    strings.inventoryEmptyTitle
                                },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (uiState.selectedTab == IngredientTab.SHOPPING_CART) {
                                    strings.cartEmptyDesc
                                } else {
                                    strings.inventoryEmptyDesc
                                },
                                fontSize = 12.sp,
                                color = AppColors.TextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            AppButton(
                                text = strings.inventorySearchBtn,
                                variant = ButtonVariant.ACCENT,
                                onClick = { viewModel.onOpenSearchCatalogDialog() },
                                height = 36.dp
                            )
                        }
                    }
                }
            } else {
                item {
                    Text(
                        text = strings.inventorySwipeToDeleteHint,
                        fontSize = 11.sp,
                        color = AppColors.TextMuted,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                items(freshIngredients, key = { it.id }) { ingredient ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                viewModel.onDeleteIngredientWithUndo(
                                    ingredient = ingredient,
                                    message = strings.itemDeletedToast(ingredient.name)
                                )
                                true
                            } else {
                                false
                            }
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        enableDismissFromEndToStart = true,
                        backgroundContent = {
                            val isSwiping = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                if (isSwiping) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(0.5f)
                                            .clip(RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
                                            .background(Color(0xFFE53935))
                                            .padding(end = 20.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = strings.delete,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = strings.delete,
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    ) {
                        IngredientItemCard(
                            ingredient = ingredient,
                            onToggleStatus = { viewModel.onToggleChecklistStatus(ingredient.id) },
                            onMarkAsConsumed = { viewModel.onMarkAsConsumed(ingredient.id) },
                            onMoveToCart = { viewModel.onMoveToCart(ingredient.id) },
                            onRestoreToStock = { viewModel.onRestoreToStock(ingredient.id) },
                            onQuantityChange = { newQty -> viewModel.onQuickUpdateQuantity(ingredient.id, newQty) },
                            onUnitChange = { newUnit -> viewModel.onQuickUpdateUnit(ingredient.id, newUnit) },
                            onEdit = { viewModel.onOpenEditDialog(ingredient) },
                            onDelete = {
                                viewModel.onDeleteIngredientWithUndo(
                                    ingredient = ingredient,
                                    message = strings.itemDeletedToast(ingredient.name)
                                )
                            }
                        )
                    }
                }

                // Dedicated Pantry Seasonings Section at the bottom of In-Stock and All tabs
                if (seasoningIngredients.isNotEmpty()) {
                    item {
                        PantrySeasoningsSection(
                            seasonings = seasoningIngredients,
                            onToggleStatus = { viewModel.onToggleChecklistStatus(it) },
                            onOpenEdit = { viewModel.onOpenEditDialog(it) }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(if (!isWeb) 80.dp else 24.dp))
            }
        }

        // Floating Undo Toast Banner at Top
        if (uiState.undoDeleteState != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = AppColors.Shadow)
                    .clip(RoundedCornerShape(20.dp))
                    .background(AppColors.PrimaryDark)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = uiState.undoDeleteState?.message ?: "",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppColors.Accent)
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { viewModel.onUndoDelete() }
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = strings.undo,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PantrySeasoningsSection(
    seasonings: List<Ingredient>,
    onToggleStatus: (String) -> Unit,
    onOpenEdit: (Ingredient) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current

    AppCard(
        modifier = modifier.fillMaxWidth(),
        padding = 16.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Kitchen,
                        contentDescription = strings.inventoryPantrySectionTitle,
                        tint = AppColors.Primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = strings.inventoryPantrySectionTitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                }

                Text(
                    text = strings.pieceCount(seasonings.size),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                seasonings.forEach { seasoning ->
                    PantrySeasoningChip(
                        seasoning = seasoning,
                        onToggleStatus = { onToggleStatus(seasoning.id) },
                        onOpenEdit = { onOpenEdit(seasoning) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PantrySeasoningChip(
    seasoning: Ingredient,
    onToggleStatus: () -> Unit,
    onOpenEdit: () -> Unit
) {
    val strings = LocalStrings.current
    val shape = RoundedCornerShape(10.dp)
    val isInStock = seasoning.status == IngredientStatus.IN_STOCK
    val isCart = seasoning.status == IngredientStatus.SHOPPING_CART
    val isConsumed = seasoning.status == IngredientStatus.CONSUMED

    val cardBg = when {
        isInStock -> AppColors.Surface
        isCart -> AppColors.AccentLight.copy(alpha = 0.35f)
        else -> AppColors.SurfaceVariant.copy(alpha = 0.5f)
    }

    val cardBorder = when {
        isInStock -> AppColors.Border
        isCart -> AppColors.Accent.copy(alpha = 0.4f)
        else -> AppColors.Border.copy(alpha = 0.4f)
    }

    val statusBg = when {
        isInStock -> AppColors.PrimaryLight
        isCart -> AppColors.Accent.copy(alpha = 0.15f)
        else -> AppColors.SurfaceVariant
    }

    val statusColor = when {
        isInStock -> AppColors.PrimaryDark
        isCart -> AppColors.Accent
        else -> AppColors.TextMuted
    }

    val statusText = when {
        isInStock -> strings.inventoryTabInStock
        isCart -> strings.inventoryTabShoppingCart
        else -> strings.inventoryStatusConsumed
    }

    val statusIcon = when {
        isInStock -> Icons.Default.Check
        isCart -> Icons.Default.ShoppingCart
        else -> Icons.Default.Remove
    }

    Row(
        modifier = Modifier
            .clip(shape)
            .background(cardBg)
            .border(0.5.dp, cardBorder, shape)
            .padding(start = 10.dp, end = 5.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = seasoning.name,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (isConsumed) AppColors.TextMuted else AppColors.TextPrimary,
            modifier = Modifier
                .pointerHoverIcon(PointerIcon.Hand)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onOpenEdit
                )
        )

        // 3-state Circular Status Toggle Button: 장바구니 > 보유중 > 소진됨
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(statusBg)
                .pointerHoverIcon(PointerIcon.Hand)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onToggleStatus
                )
                .padding(horizontal = 7.dp, vertical = 3.5.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = statusText,
                    tint = statusColor,
                    modifier = Modifier.size(11.dp)
                )
                Text(
                    text = statusText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
        }
    }
}
