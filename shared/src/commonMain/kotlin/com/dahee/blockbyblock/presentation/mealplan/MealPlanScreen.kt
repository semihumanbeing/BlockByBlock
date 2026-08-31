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
import androidx.compose.material.icons.filled.DeleteOutline
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.SurfaceVariant)
            .padding(4.dp)
    ) {
        MealPlanTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab
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
                    text = tab.title,
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
    onPromptDeleteSlot: (MealType) -> Unit
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
            // 1. Header: Plain Black Meal Type Text (hidden when unrecorded add slot) + Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isUnrecordedAddSlot) {
                    Text(
                        text = titleText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                } else {
                    Spacer(modifier = Modifier.size(1.dp))
                }

                if (hasContent) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFFEBEE))
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onDeleteClick
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = strings.delete,
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(14.dp)
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
                // [Left 50%] Bento Box Container (식단 기록 다이얼로그와 동일한 BentoLunchBoxView 컴포넌트 재사용)
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
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier.alpha(0.22f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = AppColors.TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = strings.addMealSlot,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium,
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

                // [Right 50%] List of blocks grouped by name + total ml (블록명 × N개, 총 000ml)
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
                                    Triple(first, items.size, items.sumOf { it.moldCapacityMl })
                                }
                        }
                        val totalCapacityMl = remember(slot.blocks) {
                            slot.blocks.sumOf { it.moldCapacityMl }
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(3.5.dp)
                        ) {
                            groupedBlocks.forEach { (sampleBlock, count, _) ->
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

                            Spacer(modifier = Modifier.height(1.dp))

                            // Total Capacity "총 000ml"
                            Text(
                                text = strings.totalCapacity(totalCapacityMl),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.Primary
                            )
                        }
                    } else if (!isUnrecordedAddSlot) {
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

@OptIn(ExperimentalLayoutApi::class)
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
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Week Navigator
        item {
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                padding = 10.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AppColors.SurfaceVariant)
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
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = currentWeekLabel,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AppColors.SurfaceVariant)
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
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // 7 Day Cards (Tapping on card switches to Day View for that date to edit all 4 meals)
        items(weekDays, key = { it.dateString }) { dayModel ->
            val record = dayModel.mealRecord
            val isToday = dayModel.isToday

            AppCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerHoverIcon(PointerIcon.Hand)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelectDate(dayModel.dateString) }
                    ),
                backgroundColor = if (isToday) AppColors.PrimaryLight.copy(alpha = 0.25f) else AppColors.Surface,
                borderColor = if (isToday) AppColors.Primary else AppColors.Border,
                borderWidth = if (isToday) 1.5.dp else 0.5.dp,
                padding = 14.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header: Day + Date + Total count + Hint
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isToday -> AppColors.Primary
                                            dayModel.dayOfWeekName == "일" -> Color(0xFFEF5350).copy(alpha = 0.15f)
                                            dayModel.dayOfWeekName == "토" -> Color(0xFF42A5F5).copy(alpha = 0.15f)
                                            else -> AppColors.SurfaceVariant
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayModel.dayOfWeekName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        isToday -> Color.White
                                        dayModel.dayOfWeekName == "일" -> Color(0xFFD32F2F)
                                        dayModel.dayOfWeekName == "토" -> Color(0xFF1976D2)
                                        else -> AppColors.TextPrimary
                                    }
                                )
                            }

                            Text(
                                text = "${dayModel.monthDayDisplay} (${dayModel.dayOfWeekName})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextPrimary
                            )

                            if (isToday) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(AppColors.Primary)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "TODAY",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val hasExtraMeal = record != null && (
                        record.snack.blocks.isNotEmpty() ||
                        record.snack.memo.isNotBlank() ||
                        record.snack.customTitle.isNotBlank()
                    )

                    val displayMealTypes = if (hasExtraMeal) {
                        listOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER, MealType.SNACK)
                    } else {
                        listOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER)
                    }

                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val totalWidth = maxWidth
                        val slotWidth = ((totalWidth - 20.dp) / 3).coerceAtLeast(88.dp)

                        Row(
                            modifier = if (hasExtraMeal) {
                                Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                            } else {
                                Modifier.fillMaxWidth()
                            },
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            displayMealTypes.forEach { mealType ->
                                val slot = record?.getSlot(mealType)
                                val blocks = slot?.blocks.orEmpty()
                                val hasBlocks = blocks.isNotEmpty()

                                val title = when {
                                    mealType == MealType.SNACK -> slot?.customTitle?.ifBlank { "추가" } ?: "추가"
                                    else -> strings.mealTypeName(mealType)
                                }

                                Column(
                                    modifier = if (hasExtraMeal) {
                                        Modifier.width(slotWidth)
                                    } else {
                                        Modifier.weight(1f)
                                    },
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // 1. Meal Type Header Label Above the Box (배경색 없는 검정색 라벨)
                                    Text(
                                        text = title,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.TextPrimary,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // 2. Meal Slot Box
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(68.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (hasBlocks) AppColors.SurfaceVariant.copy(alpha = 0.75f) else AppColors.SurfaceVariant.copy(alpha = 0.35f))
                                            .border(
                                                width = if (hasBlocks) 1.dp else 0.5.dp,
                                                color = if (hasBlocks) AppColors.Border else AppColors.Border.copy(alpha = 0.4f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .padding(horizontal = 4.dp, vertical = 4.dp),
                                        contentAlignment = if (hasBlocks) Alignment.CenterStart else Alignment.Center
                                    ) {
                                        if (hasBlocks) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.Start,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                blocks.forEach { item ->
                                                    FoodBlockTopView(
                                                        colorHex = item.blockColorHex,
                                                        moldCapacityMl = item.moldCapacityMl,
                                                        height = 54.dp
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                }
                                            }
                                        } else {
                                            Text(
                                                text = "-",
                                                fontSize = 15.sp,
                                                color = AppColors.TextMuted.copy(alpha = 0.4f)
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

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
