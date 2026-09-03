package com.dahee.blockbyblock.presentation.block.components

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
import com.dahee.blockbyblock.domain.model.Equipment
import com.dahee.blockbyblock.domain.model.Ingredient
import com.dahee.blockbyblock.domain.model.MoldGridPreset
import com.dahee.blockbyblock.presentation.block.BlockViewModel
import com.dahee.blockbyblock.presentation.block.state.BlockUiState
import com.dahee.blockbyblock.presentation.equipment.components.CookingToolVisual
import com.dahee.blockbyblock.presentation.equipment.components.MoldView
import androidx.compose.ui.graphics.graphicsLayer

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateBlockScreen(
    uiState: BlockUiState,
    viewModel: BlockViewModel,
    onNavigateToInventory: () -> Unit = {},
    onNavigateToEquipment: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val focusManager = LocalFocusManager.current
    var showNoIngredientBubble by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(uiState.selectedIngredientIds.size, uiState.subIngredients.size) {
        if (uiState.selectedIngredientIds.isNotEmpty() || uiState.subIngredients.isNotEmpty()) {
            showNoIngredientBubble = false
        }
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header (Back Button + Title + Subtitle)
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .pointerHoverIcon(PointerIcon.Hand)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { viewModel.onCloseCreateScreen() }
                        )
                        .padding(vertical = 4.dp, horizontal = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AppColors.TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = strings.cancel,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column {
                    Text(
                        text = if (uiState.isEditing) strings.editBlockTitle else strings.createBlockTitle,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = if (uiState.isEditing) strings.editBlockSubtitle else strings.createBlockSubtitle,
                        fontSize = 13.sp,
                        color = AppColors.TextSecondary
                    )
                }
            }

            // 2. Section 1: Menu Name & History
            item {
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    padding = 16.dp
                ) {
                    Column {
                        Text(
                            text = strings.createBlockNameLabel,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        AppTextField(
                            value = uiState.customBlockName,
                            onValueChange = { viewModel.onCustomBlockNameChange(it) },
                            placeholder = strings.createBlockNamePlaceholder,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // History Recommendation Chips (placed right below the name input)
                        if (uiState.historyBlocks.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(uiState.historyBlocks, key = { it.id }) { history ->
                                    val isSelected = uiState.customBlockName == history.name
                                    AppChip(
                                        text = history.name,
                                        selected = isSelected,
                                        onClick = { viewModel.onApplyHistoryBlock(history) },
                                        horizontalPadding = 10.dp,
                                        verticalPadding = 6.dp,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Section 2: 3D Food Block Hero Preview with Vertical Color Palette on the side
            item {
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    padding = 16.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Center: 3D Food Block Preview
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(190.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            FoodBlock3DView(
                                colorHex = uiState.selectedBlockColorHex,
                                moldCapacityMl = uiState.selectedMold?.displayCapacity ?: 250,
                                size = 160.dp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Side: Vertical Color Palette (5 core food block colors)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(AppColors.SurfaceVariant)
                                .border(1.dp, AppColors.Border, RoundedCornerShape(16.dp))
                                .padding(horizontal = 8.dp, vertical = 10.dp)
                        ) {
                            com.dahee.blockbyblock.domain.model.FoodBlockPalette.options.forEach { option ->
                                val isSelected = uiState.selectedBlockColorHex.equals(option.hex, ignoreCase = true)
                                val dotColor = AppColors.hexToColor(option.hex)

                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(dotColor)
                                        .border(
                                            width = if (isSelected) 2.5.dp else 1.dp,
                                            color = if (isSelected) AppColors.PrimaryDark else AppColors.Border,
                                            shape = CircleShape
                                        )
                                        .pointerHoverIcon(PointerIcon.Hand)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = { viewModel.onBlockColorChange(option.hex) }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = option.nameKo,
                                            tint = if (option.hex == "#F5F5F0" || option.hex == "#FDD835") Color(0xFF2E2E2E) else Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Section 3: Select Mold & Number of Molds to Use + Portion Quantity
            item {
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    padding = 16.dp
                ) {
                    Column {
                        Text(
                            text = strings.createBlockSectionMold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (uiState.availableMolds.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = strings.createBlockNoMolds,
                                    fontSize = 13.sp,
                                    color = AppColors.TextSecondary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                AppButton(
                                    text = strings.createBlockGoToEquipment,
                                    onClick = onNavigateToEquipment,
                                    variant = ButtonVariant.SECONDARY
                                )
                            }
                        } else {
                            // Horizontal scrolling cards for registered molds
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(uiState.availableMolds, key = { it.id }) { mold ->
                                    val isSelected = uiState.selectedMoldId == mold.id
                                    MoldSelectCard(
                                        mold = mold,
                                        isSelected = isSelected,
                                        onClick = { viewModel.onSelectMold(mold.id) }
                                    )
                                }
                            }

                            // Steppers: Number of molds to use + Portion quantity directly below
                            if (uiState.selectedMold != null) {
                                val mold = uiState.selectedMold!!
                                val maxCount = mold.quantity.coerceAtLeast(1)

                                Spacer(modifier = Modifier.height(16.dp))

                                // Stepper: Number of molds to use
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = strings.createBlockMoldCountLabel,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = AppColors.TextPrimary
                                        )
                                        Text(
                                            text = strings.createBlockMoldCountMax(maxCount),
                                            fontSize = 12.sp,
                                            color = AppColors.TextSecondary
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(AppColors.SurfaceVariant)
                                                .border(1.dp, AppColors.Border, CircleShape)
                                                .pointerHoverIcon(PointerIcon.Hand)
                                                .clickable(
                                                    enabled = uiState.selectedMoldCount > 1,
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null,
                                                    onClick = { viewModel.onMoldCountChange(-1) }
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Remove,
                                                contentDescription = "Minus",
                                                tint = if (uiState.selectedMoldCount > 1) AppColors.TextPrimary else AppColors.TextMuted,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        Text(
                                            text = strings.pieceCount(uiState.selectedMoldCount),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AppColors.TextPrimary,
                                            modifier = Modifier.width(36.dp),
                                            textAlign = TextAlign.Center
                                        )

                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(if (uiState.selectedMoldCount < maxCount) AppColors.Primary else AppColors.SurfaceVariant)
                                                .pointerHoverIcon(PointerIcon.Hand)
                                                .clickable(
                                                    enabled = uiState.selectedMoldCount < maxCount,
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null,
                                                    onClick = { viewModel.onMoldCountChange(1) }
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Plus",
                                                tint = if (uiState.selectedMoldCount < maxCount) Color.White else AppColors.TextMuted,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Stepper: Total Block Count (총 블록 개수)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = strings.createBlockQuantityLabel,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = AppColors.TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = strings.blockYieldCalculation(
                                                uiState.selectedMoldCount,
                                                mold.cellCount,
                                                uiState.selectedMoldCount * mold.cellCount
                                            ),
                                            fontSize = 11.5.sp,
                                            color = AppColors.Primary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(AppColors.SurfaceVariant)
                                                .border(1.dp, AppColors.Border, CircleShape)
                                                .pointerHoverIcon(PointerIcon.Hand)
                                                .clickable(
                                                    enabled = uiState.blockQuantity > 1,
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null,
                                                    onClick = { viewModel.onBlockQuantityChange(-1) }
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Remove,
                                                contentDescription = "Minus",
                                                tint = if (uiState.blockQuantity > 1) AppColors.TextPrimary else AppColors.TextMuted,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        Text(
                                            text = strings.pieceCount(uiState.blockQuantity),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AppColors.TextPrimary,
                                            modifier = Modifier.width(36.dp),
                                            textAlign = TextAlign.Center
                                        )

                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(AppColors.Primary)
                                                .pointerHoverIcon(PointerIcon.Hand)
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null,
                                                    onClick = { viewModel.onBlockQuantityChange(1) }
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Plus",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Section 4: Ingredients (Main Ingredients + Extra Ingredients in one unified card)
            item {
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    padding = 16.dp
                ) {
                    Column {
                        // Part A: Main Ingredients
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = strings.createBlockSectionIngredients,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.TextPrimary
                                )
                                if (uiState.selectedIngredientIds.isNotEmpty()) {
                                    Text(
                                        text = strings.selectedIngredientsCount(uiState.selectedIngredientIds.size),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.Primary
                                    )
                                }
                            }

                            Text(
                                text = strings.deliveryFoodHint,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                color = AppColors.TextMuted,
                                textAlign = TextAlign.End,
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .padding(start = 12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (uiState.inStockMainIngredients.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = strings.createBlockNoIngredients,
                                    fontSize = 13.sp,
                                    color = AppColors.TextSecondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                AppButton(
                                    text = strings.createBlockGoToInventory,
                                    onClick = onNavigateToInventory,
                                    variant = ButtonVariant.SECONDARY
                                )
                            }
                        } else {
                            // Search bar for main ingredients
                            AppTextField(
                                value = uiState.ingredientSearchQuery,
                                onValueChange = { viewModel.onIngredientSearchQueryChange(it) },
                                placeholder = strings.createBlockSearchIngredientPlaceholder,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = AppColors.TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Selected Ingredients Summary Tags (Visible across pagination)
                            val selectedMainIngredients = remember(uiState.selectedIngredientIds, uiState.inStockMainIngredients) {
                                uiState.inStockMainIngredients.filter { uiState.selectedIngredientIds.contains(it.id) }
                            }
                            if (selectedMainIngredients.isNotEmpty()) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    selectedMainIngredients.forEach { ing ->
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(AppColors.PrimaryLight.copy(alpha = 0.7f))
                                                .border(1.dp, AppColors.Primary, RoundedCornerShape(8.dp))
                                                .pointerHoverIcon(PointerIcon.Hand)
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null,
                                                    onClick = { viewModel.onToggleIngredient(ing.id) }
                                                )
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "#${ing.name}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AppColors.PrimaryDark
                                            )
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove",
                                                tint = AppColors.PrimaryDark,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            if (uiState.filteredInStockIngredients.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = strings.createBlockNoMatchingIngredients,
                                        fontSize = 13.sp,
                                        color = AppColors.TextSecondary
                                    )
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    uiState.pagedInStockIngredients.forEach { ingredient ->
                                        val isSelected = uiState.selectedIngredientIds.contains(ingredient.id)
                                        IngredientCheckboxRow(
                                            ingredient = ingredient,
                                            isSelected = isSelected,
                                            onToggle = { viewModel.onToggleIngredient(ingredient.id) }
                                        )
                                    }
                                }

                                if (uiState.totalIngredientPages > 1) {
                                    IngredientPaginationControls(
                                        currentPage = uiState.ingredientPageIndex,
                                        totalPages = uiState.totalIngredientPages,
                                        onPageChange = { viewModel.onIngredientPageChange(it) }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Part B: Extra Ingredients (추가 재료)
                        Text(
                            text = strings.createBlockSectionSubIngredients,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.TextPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // 1. Direct sub-ingredient input text field at Top (Enter key support)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppTextField(
                                value = uiState.subIngredientInput,
                                onValueChange = { viewModel.onSubIngredientInputChange(it) },
                                placeholder = strings.createBlockSubIngredientPlaceholder,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { viewModel.onAddSubIngredient() }),
                                modifier = Modifier.weight(1f)
                            )

                            AppButton(
                                text = strings.createBlockAddSubBtn,
                                onClick = { viewModel.onAddSubIngredient() },
                                enabled = uiState.subIngredientInput.isNotBlank(),
                                variant = ButtonVariant.PRIMARY
                            )
                        }

                        // 2. Chip list at Bottom (In-place toggleable chips without redundant bottom tag list)
                        val inStockNames = uiState.inStockSeasonings.map { it.name }
                        val customAddedNames = uiState.subIngredients.filter { !inStockNames.contains(it) }
                        val allDisplayItems = inStockNames + customAddedNames

                        if (allDisplayItems.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                allDisplayItems.forEach { name ->
                                    val isSelected = uiState.subIngredients.contains(name)
                                    val isCustom = customAddedNames.contains(name)

                                    SubIngredientToggleChip(
                                        name = name,
                                        isSelected = isSelected,
                                        isCustom = isCustom,
                                        onToggle = {
                                            if (isSelected) {
                                                viewModel.onRemoveSubIngredient(name)
                                            } else {
                                                viewModel.onAddSubIngredientDirectly(name)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 7. Section 6: Other Settings (Cooking Tool Selection + Shelf Life Days)
            item {
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    padding = 16.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = strings.createBlockSectionOther,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )

                        // Part 1: Cooking Tool Selection (Optional)
                        Column {
                            Text(
                                text = strings.createBlockSectionCookingTool,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.TextPrimary
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            val toolOptions = remember(uiState.availableCookingTools) {
                                uiState.availableCookingTools.mapNotNull { it.toolType }.distinct()
                            }

                            if (toolOptions.isNotEmpty()) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.Start),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    toolOptions.forEach { toolType ->
                                        val isSelected = uiState.selectedCookingToolTypes.contains(toolType)
                                        val toolName = strings.cookingToolName(toolType)

                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier
                                                .width(72.dp)
                                                .pointerHoverIcon(PointerIcon.Hand)
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null,
                                                    onClick = { viewModel.onToggleCookingTool(toolType) }
                                                )
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.TopEnd
                                            ) {
                                                CookingToolVisual(
                                                    type = toolType,
                                                    size = 50.dp,
                                                    modifier = Modifier.graphicsLayer {
                                                        alpha = if (isSelected) 1.0f else 0.42f
                                                        scaleX = if (isSelected) 1.06f else 0.94f
                                                        scaleY = if (isSelected) 1.06f else 0.94f
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
                                                            contentDescription = "Selected",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(12.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text(
                                                text = toolName,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) AppColors.PrimaryDark else AppColors.TextSecondary,
                                                textAlign = TextAlign.Center,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }

                                // Multi-Tool Specific Settings: One row per selected tool
                                if (uiState.selectedCookingTools.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        uiState.selectedCookingTools.forEach { draft ->
                                            val toolName = strings.cookingToolName(draft.toolType)
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(AppColors.SurfaceVariant.copy(alpha = 0.45f))
                                                    .border(0.8.dp, AppColors.Border.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (draft.toolType == com.dahee.blockbyblock.domain.model.CookingToolType.BLENDER) {
                                                    // Blender: Pure prep tool, no timer needed
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        CookingToolVisual(type = draft.toolType, size = 22.dp)
                                                        Text(
                                                            text = "$toolName (재료 갈아서 소분)",
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = AppColors.TextPrimary
                                                        )
                                                    }

                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(AppColors.PrimaryLight.copy(alpha = 0.6f))
                                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Text(
                                                            text = "전처리 도구",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = AppColors.PrimaryDark
                                                        )
                                                    }
                                                } else {
                                                    // Heating / Reheating Tools: Show Temp & Time Inputs
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        CookingToolVisual(type = draft.toolType, size = 22.dp)
                                                        Text(
                                                            text = "$toolName ${strings.cookingTimeLabel}",
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = AppColors.TextPrimary
                                                        )
                                                    }

                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        // If Oven, Air Fryer, Slow Cooker: Show Temperature (°C)
                                                        if (draft.toolType == com.dahee.blockbyblock.domain.model.CookingToolType.OVEN ||
                                                            draft.toolType == com.dahee.blockbyblock.domain.model.CookingToolType.AIR_FRYER ||
                                                            draft.toolType == com.dahee.blockbyblock.domain.model.CookingToolType.SLOW_COOKER
                                                        ) {
                                                            CompactCookingValueField(
                                                                value = draft.temperatureInput,
                                                                onValueChange = { viewModel.onCookingToolTempChange(draft.toolType, it) },
                                                                suffix = strings.temperatureUnitCelsius,
                                                                placeholder = if (draft.toolType == com.dahee.blockbyblock.domain.model.CookingToolType.SLOW_COOKER) "90" else "180",
                                                                width = 48.dp
                                                            )
                                                        }

                                                        // Minutes Input
                                                        CompactCookingValueField(
                                                            value = draft.timeMinutesInput,
                                                            onValueChange = { viewModel.onCookingToolMinutesChange(draft.toolType, it) },
                                                            suffix = strings.timeUnitMinutes,
                                                            placeholder = if (draft.toolType == com.dahee.blockbyblock.domain.model.CookingToolType.SLOW_COOKER) "120" else if (draft.toolType == com.dahee.blockbyblock.domain.model.CookingToolType.MICROWAVE) "3" else "15",
                                                            width = 44.dp
                                                        )

                                                        // If Microwave: Show Seconds Input
                                                        if (draft.toolType == com.dahee.blockbyblock.domain.model.CookingToolType.MICROWAVE) {
                                                            CompactCookingValueField(
                                                                value = draft.timeSecondsInput,
                                                                onValueChange = { viewModel.onCookingToolSecondsChange(draft.toolType, it) },
                                                                suffix = strings.timeUnitSeconds,
                                                                placeholder = "30",
                                                                width = 44.dp
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                Text(
                                    text = strings.noOwnedCookingTools,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.TextMuted,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Part 2: Shelf Life (Editable input for days)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.createBlockShelfLifeLabel,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.TextPrimary
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                AppTextField(
                                    value = uiState.shelfLifeDaysInput,
                                    onValueChange = { viewModel.onShelfLifeDaysInputChange(it) },
                                    placeholder = "90",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.width(76.dp)
                                )
                                Text(
                                    text = strings.unitDay,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // 6. Submit Button with Tooltip Speech Bubble (Overlaid on top without pushing button down)
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    // Actual Submit Button (Stationary)
                    AppButton(
                        text = if (uiState.isEditing) strings.editBlockSubmitBtn else strings.createBlockSubmitBtn,
                        onClick = {
                            val hasNoIngredients = uiState.selectedIngredientIds.isEmpty() && uiState.subIngredients.isEmpty()
                            if (hasNoIngredients && !showNoIngredientBubble) {
                                showNoIngredientBubble = true
                            } else {
                                showNoIngredientBubble = false
                                viewModel.onSubmitCreateBlock()
                            }
                        },
                        enabled = uiState.canSubmit,
                        variant = ButtonVariant.PRIMARY,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Floating Speech Bubble (Overlaps the card above smoothly)
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showNoIngredientBubble,
                        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(initialScale = 0.85f),
                        exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut(targetScale = 0.85f),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-44).dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .shadow(6.dp, RoundedCornerShape(10.dp), spotColor = AppColors.Shadow)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AppColors.TextPrimary)
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = strings.createBlockNoIngredientTooltip,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }

                            // Speech Bubble Tail (Downward Triangle)
                            androidx.compose.foundation.Canvas(
                                modifier = Modifier.size(width = 12.dp, height = 6.dp)
                            ) {
                                val path = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(0f, 0f)
                                    lineTo(size.width, 0f)
                                    lineTo(size.width / 2f, size.height)
                                    close()
                                }
                                drawPath(path, color = AppColors.TextPrimary)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
private fun IngredientCheckboxRow(
    ingredient: Ingredient,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    val bgColor = if (isSelected) AppColors.PrimaryLight.copy(alpha = 0.5f) else AppColors.Surface
    val borderColor = if (isSelected) AppColors.Primary else AppColors.Border

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(bgColor)
            .border(if (isSelected) 1.5.dp else 0.5.dp, borderColor, shape)
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = ingredient.name,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) AppColors.PrimaryDark else AppColors.TextPrimary
        )

        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (isSelected) AppColors.Primary else Color.Transparent)
                .border(
                    width = if (isSelected) 0.dp else 1.5.dp,
                    color = if (isSelected) Color.Transparent else AppColors.Border,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}


@Composable
private fun MoldSelectCard(
    mold: Equipment,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    val strings = LocalStrings.current
    val bgColor = if (isSelected) AppColors.PrimaryLight.copy(alpha = 0.5f) else AppColors.Surface
    val borderColor = if (isSelected) AppColors.Primary else AppColors.Border
    val borderWidth = if (isSelected) 2.dp else 0.5.dp

    Box(
        modifier = Modifier
            .width(130.dp)
            .clip(shape)
            .background(bgColor)
            .border(borderWidth, borderColor, shape)
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            MoldView(
                preset = mold.moldPreset ?: MoldGridPreset.CUSTOM,
                moldColor = AppColors.hexToColor(mold.moldColorHex),
                cellCount = mold.cellCount,
                size = 54.dp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${mold.displayCapacity}ml",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) AppColors.PrimaryDark else AppColors.TextPrimary
            )

            Text(
                text = strings.slotCount(mold.cellCount),
                fontSize = 11.sp,
                color = if (isSelected) AppColors.Primary else AppColors.TextSecondary
            )
        }
    }
}

@Composable
private fun IngredientPaginationControls(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit
) {
    val strings = LocalStrings.current
    val hasPrev = currentPage > 0
    val hasNext = currentPage < totalPages - 1

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Prev Page Button
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (hasPrev) AppColors.SurfaceVariant else Color.Transparent)
                .border(
                    width = 0.5.dp,
                    color = if (hasPrev) AppColors.Border else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                )
                .pointerHoverIcon(if (hasPrev) PointerIcon.Hand else PointerIcon.Default)
                .clickable(
                    enabled = hasPrev,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onPageChange(currentPage - 1) }
                )
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = strings.prevPageBtn,
                tint = if (hasPrev) AppColors.TextPrimary else AppColors.TextMuted,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = strings.prevPageBtn,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (hasPrev) AppColors.TextPrimary else AppColors.TextMuted
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Page Indicator
        Text(
            text = "${currentPage + 1} / $totalPages",
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
                .padding(horizontal = 10.dp, vertical = 6.dp),
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
private fun SubIngredientToggleChip(
    name: String,
    isSelected: Boolean,
    isCustom: Boolean,
    onToggle: () -> Unit
) {
    val shape = RoundedCornerShape(8.dp)
    val bgColor = if (isSelected) AppColors.PrimaryLight.copy(alpha = 0.7f) else AppColors.Surface
    val borderColor = if (isSelected) AppColors.Primary else AppColors.Border
    val textColor = if (isSelected) AppColors.PrimaryDark else AppColors.TextPrimary

    Row(
        modifier = Modifier
            .clip(shape)
            .background(bgColor)
            .border(if (isSelected) 1.5.dp else 0.5.dp, borderColor, shape)
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle
            )
            .padding(horizontal = 9.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = if (isSelected) {
                if (isCustom) Icons.Default.Close else Icons.Default.Check
            } else {
                Icons.Default.Add
            },
            contentDescription = if (isSelected) (if (isCustom) "Delete" else "Selected") else "Add",
            tint = if (isSelected) AppColors.PrimaryDark else AppColors.TextSecondary,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = name,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
private fun CompactCookingValueField(
    value: String,
    onValueChange: (String) -> Unit,
    suffix: String,
    placeholder: String = "",
    width: androidx.compose.ui.unit.Dp = 44.dp,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .width(width)
                .height(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AppColors.Surface)
                .border(1.dp, AppColors.Border, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = AppColors.TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                cursorBrush = SolidColor(AppColors.Primary),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                decorationBox = { innerTextField ->
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            color = AppColors.TextMuted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    innerTextField()
                }
            )
        }
        Text(
            text = suffix,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.TextSecondary
        )
    }
}
