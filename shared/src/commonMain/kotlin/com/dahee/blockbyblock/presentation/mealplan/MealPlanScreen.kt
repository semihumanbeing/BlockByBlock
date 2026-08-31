package com.dahee.blockbyblock.presentation.mealplan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dahee.blockbyblock.core.i18n.LocalStrings
import com.dahee.blockbyblock.core.theme.AppColors
import com.dahee.blockbyblock.core.ui.AppCard
import com.dahee.blockbyblock.domain.model.DayMealRecord
import com.dahee.blockbyblock.domain.model.MealBlockItem
import com.dahee.blockbyblock.domain.model.MealSlotRecord
import com.dahee.blockbyblock.domain.model.MealType
import com.dahee.blockbyblock.presentation.block.components.FoodBlock3DView
import com.dahee.blockbyblock.presentation.block.components.FoodBlockTopView
import com.dahee.blockbyblock.presentation.mealplan.components.BentoLunchBoxView
import com.dahee.blockbyblock.presentation.mealplan.components.MealRecordDialog

@Composable
fun MealPlanScreen(
    viewModel: MealPlanViewModel,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    // Delete Confirmation Dialog State
    var pendingDeleteMealType by remember { mutableStateOf<MealType?>(null) }
    var pendingDeleteDateString by remember { mutableStateOf<String?>(null) }

    // Meal Slot Edit Dialog
    if (uiState.isSlotDialogOpen) {
        MealRecordDialog(
            mealType = uiState.editingMealType,
            dateLabel = uiState.editingDateLabel,
            selectedBlocks = uiState.slotSelectedBlocks,
            availableBlocks = uiState.slotAvailableBlocks,
            titleInput = uiState.slotTitleInput,
            onTitleChange = { viewModel.onTitleInputChange(it) },
            memoInput = uiState.slotMemoInput,
            onMemoChange = { viewModel.onMemoInputChange(it) },
            onMoveToTop = { viewModel.onMoveBlockToTop(it) },
            onMoveToBottom = { viewModel.onMoveBlockToBottom(it) },
            savedPresets = uiState.savedPresets,
            onSaveCurrentAsPreset = { viewModel.onSaveCurrentAsPreset(it) },
            onApplyPreset = { viewModel.onApplyPreset(it) },
            onDeletePreset = { viewModel.onDeletePreset(it) },
            onSave = { viewModel.onSaveSlot() },
            onDismiss = { viewModel.onCloseSlotDialog() }
        )
    }

    // Delete Confirmation Modal
    if (pendingDeleteMealType != null && pendingDeleteDateString != null) {
        val targetMealType = pendingDeleteMealType!!
        val targetDateStr = pendingDeleteDateString!!

        AlertDialog(
            onDismissRequest = {
                pendingDeleteMealType = null
                pendingDeleteDateString = null
            },
            title = {
                Text(
                    text = strings.deleteMealRecordTitle,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
            },
            text = {
                Text(
                    text = strings.deleteMealRecordConfirm(strings.mealTypeName(targetMealType)),
                    fontSize = 14.sp,
                    color = AppColors.TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onDeleteSlot(targetDateStr, targetMealType)
                        pendingDeleteMealType = null
                        pendingDeleteDateString = null
                    }
                ) {
                    Text(
                        text = strings.delete,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE53935)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pendingDeleteMealType = null
                        pendingDeleteDateString = null
                    }
                ) {
                    Text(
                        text = strings.cancel,
                        fontSize = 14.sp,
                        color = AppColors.TextSecondary
                    )
                }
            },
            containerColor = AppColors.Surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
            }
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // 1. Top Segmented Tab Switcher [오늘/일별 식단] | [이번 주]
        MealPlanTabSwitcher(
            selectedTab = uiState.selectedTab,
            onSelectTab = { viewModel.onSelectTab(it) }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Tab Content
        when (uiState.selectedTab) {
            MealPlanTab.TODAY -> TodayMealView(
                selectedDateFormatted = uiState.selectedDateFormatted,
                selectedDateString = uiState.selectedDateString,
                isSelectedDateToday = uiState.isSelectedDateToday,
                currentDayRecord = uiState.currentDayMealRecord,
                onPreviousDay = { viewModel.onPreviousDay() },
                onNextDay = { viewModel.onNextDay() },
                onResetToToday = { viewModel.onResetToToday() },
                onOpenSlot = { mealType ->
                    viewModel.onOpenSlotDialog(
                        dateString = uiState.selectedDateString,
                        dateLabel = uiState.selectedDateFormatted,
                        mealType = mealType
                    )
                },
                onPromptDeleteSlot = { mealType ->
                    pendingDeleteMealType = mealType
                    pendingDeleteDateString = uiState.selectedDateString
                },
                onSavePresetSlot = { mealType ->
                    viewModel.onSaveSlotAsPreset(uiState.selectedDateString, mealType)
                }
            )
            MealPlanTab.WEEK -> WeekMealView(
                currentWeekLabel = uiState.currentWeekLabel,
                weekDays = uiState.weekDays,
                onPreviousWeek = { viewModel.onPreviousWeek() },
                onNextWeek = { viewModel.onNextWeek() },
                onCurrentWeek = { viewModel.onCurrentWeek() },
                onSelectDate = { dateString ->
                    viewModel.onSelectDateAndOpenDayView(dateString)
                },
                onOpenDaySlot = { dateString, dateLabel, mealType ->
                    viewModel.onOpenSlotDialog(dateString, dateLabel, mealType)
                }
            )
        }
    }
}

@Composable
private fun MealPlanTabSwitcher(
    selectedTab: MealPlanTab,
    onSelectTab: (MealPlanTab) -> Unit
) {
    val strings = LocalStrings.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.SurfaceVariant)
            .padding(4.dp)
    ) {
        MealPlanTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab
            val tabTitle = when (tab) {
                MealPlanTab.TODAY -> strings.mealPlanTabDaily
                MealPlanTab.WEEK -> strings.mealPlanTabWeekly
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) AppColors.Surface else Color.Transparent)
                    .border(
                        width = if (isSelected) 0.5.dp else 0.dp,
                        color = if (isSelected) AppColors.Border else Color.Transparent,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .pointerHoverIcon(PointerIcon.Hand)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelectTab(tab) }
                    )
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tabTitle,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) AppColors.PrimaryDark else AppColors.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun TodayMealView(
    selectedDateFormatted: String,
    selectedDateString: String,
    isSelectedDateToday: Boolean,
    currentDayRecord: DayMealRecord?,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onResetToToday: () -> Unit,
    onOpenSlot: (MealType) -> Unit,
    onPromptDeleteSlot: (MealType) -> Unit,
    onSavePresetSlot: (MealType) -> Unit
) {
    val strings = LocalStrings.current
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Date Navigator (Clean flat header bar without white elevated card)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous day button
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(AppColors.SurfaceVariant.copy(alpha = 0.8f))
                    .pointerHoverIcon(PointerIcon.Hand)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onPreviousDay
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous Day",
                    tint = AppColors.TextPrimary,
                    modifier = Modifier.size(15.dp)
                )
            }

            // Date Label & Today indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = selectedDateFormatted,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )

                if (!isSelectedDateToday) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AppColors.PrimaryLight)
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onResetToToday
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = strings.backToToday,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.PrimaryDark
                        )
                    }
                }
            }

            // Next day button
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(AppColors.SurfaceVariant.copy(alpha = 0.8f))
                    .pointerHoverIcon(PointerIcon.Hand)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onNextDay
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next Day",
                    tint = AppColors.TextPrimary,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        // 4 Meal Slots: Breakfast, Lunch, Dinner, Snack closely positioned
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MealType.entries.forEach { mealType ->
                val slotRecord = currentDayRecord?.getSlot(mealType) ?: MealSlotRecord(mealType)
                MealSlotCard(
                    mealType = mealType,
                    slot = slotRecord,
                    onClick = { onOpenSlot(mealType) },
                    onSavePresetClick = { onSavePresetSlot(mealType) },
                    onDeleteClick = { onPromptDeleteSlot(mealType) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MealSlotCard(
    mealType: MealType,
    slot: MealSlotRecord,
    onClick: () -> Unit,
    onSavePresetClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val hasBlocks = slot.blocks.isNotEmpty()
    val hasMemo = slot.memo.isNotBlank()
    val hasContent = hasBlocks || hasMemo
    val isSnack = mealType == MealType.SNACK
    val isUnrecordedAddSlot = isSnack && !hasContent

    val titleText = when {
        isUnrecordedAddSlot -> strings.addMealSlot
        isSnack && slot.customTitle.isNotBlank() -> slot.customTitle
        else -> strings.mealTypeName(mealType)
    }

    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        backgroundColor = if (hasBlocks) AppColors.SurfaceVariant.copy(alpha = 0.35f) else AppColors.SurfaceVariant.copy(alpha = 0.18f),
        borderColor = if (isUnrecordedAddSlot) AppColors.Border.copy(alpha = 0.25f) else AppColors.Border.copy(alpha = 0.45f),
        borderWidth = 0.5.dp,
        elevation = 0.dp,
        cornerRadius = 14.dp,
        padding = 8.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Header: Plain Black Meal Type Text (or disabled '식사 추가' on top-left) + Save Preset + Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = titleText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnrecordedAddSlot) AppColors.TextMuted.copy(alpha = 0.7f) else AppColors.TextPrimary
                )

                if (hasContent) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (hasBlocks) {
                            Text(
                                text = strings.saveMealPreset,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.Primary,
                                modifier = Modifier
                                    .pointerHoverIcon(PointerIcon.Hand)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = onSavePresetClick
                                    )
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = strings.delete,
                            tint = AppColors.TextMuted,
                            modifier = Modifier
                                .size(18.dp)
                                .pointerHoverIcon(PointerIcon.Hand)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onDeleteClick
                                )
                        )
                    }
                }
            }

            // 2. Main Content: [Left 50%: Authentic Bento Box with Food Blocks] + [Right 50%: Block list line by line in order]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // [Left 50%] Bento Box Container
                BentoLunchBoxView(
                    blocks = slot.blocks,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    blockHeight = 76.dp,
                    emptyPlaceholder = {
                        if (isUnrecordedAddSlot) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier.alpha(0.55f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = AppColors.TextMuted,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = strings.addMealSlot,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.TextMuted
                                )
                            }
                        } else {
                            Text(
                                text = "+",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextMuted.copy(alpha = 0.5f)
                            )
                        }
                    }
                )

                // [Right 50%] List of blocks grouped by name + total ml
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    if (hasBlocks) {
                        val groupedBlocks = remember(slot.blocks) {
                            slot.blocks.groupBy { it.blockId to it.moldCapacityMl }
                                .map { (_, items) ->
                                    val first = items.first()
                                    Pair(first, items.size)
                                }
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            groupedBlocks.forEach { (sampleBlock, count) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    // Color Indicator
                                    Box(
                                        modifier = Modifier
                                            .size(9.dp)
                                            .clip(CircleShape)
                                            .background(AppColors.hexToColor(sampleBlock.blockColorHex))
                                            .border(0.5.dp, Color.Black.copy(alpha = 0.15f), CircleShape)
                                    )

                                    // Block Name
                                    Text(
                                        text = sampleBlock.blockName,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )

                                    // Quantity "× N개"
                                    Text(
                                        text = strings.blockCountSuffix(count),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppColors.TextSecondary
                                    )
                                }
                            }
                        }
                    } else if (isUnrecordedAddSlot) {
                        Text(
                            text = strings.addMealSlot,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextMuted.copy(alpha = 0.45f)
                        )
                    } else {
                        Text(
                            text = strings.mealPlanHint,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.TextMuted
                        )
                    }
                }
            }

            // 3. Memo if present
            if (hasMemo) {
                Text(
                    text = strings.memoPrefix(slot.memo),
                    fontSize = 11.sp,
                    color = AppColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun WeekMealView(
    currentWeekLabel: String,
    weekDays: List<DayMealPlanUiModel>,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onCurrentWeek: () -> Unit,
    onSelectDate: (String) -> Unit,
    onOpenDaySlot: (String, String, MealType) -> Unit
) {
    val strings = LocalStrings.current

    // Check if any day in the week has an extra meal/snack recorded
    val hasAnyExtraMeal = weekDays.any { day ->
        val record = day.mealRecord
        record != null && (
            record.snack.blocks.isNotEmpty() ||
            record.snack.memo.isNotBlank() ||
            record.snack.customTitle.isNotBlank()
        )
    }

    val displayMealTypes = if (hasAnyExtraMeal) {
        listOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER, MealType.SNACK)
    } else {
        listOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 1. Slim Week Navigator (Flat Header bar without white elevated AppCard)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(AppColors.SurfaceVariant.copy(alpha = 0.8f))
                    .pointerHoverIcon(PointerIcon.Hand)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onPreviousWeek
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous Week",
                    tint = AppColors.TextPrimary,
                    modifier = Modifier.size(15.dp)
                )
            }

            Text(
                text = currentWeekLabel,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(AppColors.SurfaceVariant.copy(alpha = 0.8f))
                    .pointerHoverIcon(PointerIcon.Hand)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onNextWeek
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next Week",
                    tint = AppColors.TextPrimary,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        // 2. 7 Days Compact Bento Rows (일별 식단과 동일한 은은한 배경 톤 & 미세 테두리)
        weekDays.forEach { dayModel ->
            val record = dayModel.mealRecord
            val isToday = dayModel.isToday
            val hasBlocksInDay = record != null && (
                record.breakfast.blocks.isNotEmpty() ||
                record.lunch.blocks.isNotEmpty() ||
                record.dinner.blocks.isNotEmpty() ||
                record.snack.blocks.isNotEmpty()
            )

            AppCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .pointerHoverIcon(PointerIcon.Hand)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelectDate(dayModel.dateString) }
                    ),
                backgroundColor = when {
                    isToday -> AppColors.PrimaryLight.copy(alpha = 0.35f)
                    hasBlocksInDay -> AppColors.SurfaceVariant.copy(alpha = 0.35f)
                    else -> AppColors.SurfaceVariant.copy(alpha = 0.18f)
                },
                borderColor = if (isToday) AppColors.Primary else AppColors.Border.copy(alpha = 0.45f),
                borderWidth = if (isToday) 1.5.dp else 0.5.dp,
                elevation = 0.dp,
                cornerRadius = 14.dp,
                padding = 6.dp
            ) {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val computedHeight = (maxHeight - 8.dp).coerceIn(34.dp, 56.dp)

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Left: Date Badge (요일 + 월/일)
                        Column(
                            modifier = Modifier
                                .width(50.dp)
                                .fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = dayModel.dayOfWeekName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    isToday -> AppColors.Primary
                                    dayModel.dayOfWeekName == "일" -> Color(0xFFD32F2F)
                                    dayModel.dayOfWeekName == "토" -> Color(0xFF1976D2)
                                    else -> AppColors.TextPrimary
                                }
                            )
                            Text(
                                text = dayModel.monthDayDisplay,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isToday) AppColors.Primary else AppColors.TextSecondary
                            )
                        }

                        // Right: Authentic Bento Slots (아침 / 점심 / 저녁 / 추가 - BentoLunchBoxView 공용 컴포넌트 재사용)
                        displayMealTypes.forEach { mealType ->
                            val slot = record?.getSlot(mealType)
                            val blocks = slot?.blocks.orEmpty()

                            BentoLunchBoxView(
                                blocks = blocks,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                emptyPlaceholder = {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(AppColors.TextMuted.copy(alpha = 0.3f))
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
    }
}
