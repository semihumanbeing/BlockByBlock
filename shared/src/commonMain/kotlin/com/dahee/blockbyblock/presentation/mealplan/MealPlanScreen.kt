package com.dahee.blockbyblock.presentation.mealplan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
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
import com.dahee.blockbyblock.presentation.block.components.LegoTopViewBlock
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
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Date Navigator (Left: Previous day / Right: Next day)
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
                    // Previous day button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AppColors.SurfaceVariant)
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
                            modifier = Modifier.size(16.dp)
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
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AppColors.SurfaceVariant)
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
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Subtitle hint
        item {
            Text(
                text = strings.mealPlanHint,
                fontSize = 12.sp,
                color = AppColors.TextSecondary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        // 4 Meal Slots: Breakfast, Lunch, Dinner, Snack
        items(MealType.entries.toTypedArray(), key = { it.name }) { mealType ->
            val slotRecord = currentDayRecord?.getSlot(mealType) ?: MealSlotRecord(mealType)
            MealSlotCard(
                mealType = mealType,
                slot = slotRecord,
                onClick = { onOpenSlot(mealType) },
                onDeleteClick = { onPromptDeleteSlot(mealType) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MealSlotCard(
    mealType: MealType,
    slot: MealSlotRecord,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val strings = LocalStrings.current
    val hasBlocks = slot.blocks.isNotEmpty()
    val hasMemo = slot.memo.isNotBlank()
    val hasContent = hasBlocks || hasMemo

    val badgeColor = when (mealType) {
        MealType.BREAKFAST -> Color(0xFFFF9800)
        MealType.LUNCH -> Color(0xFF4CAF50)
        MealType.DINNER -> Color(0xFF3F51B5)
        MealType.SNACK -> Color(0xFF9C27B0)
    }

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        borderColor = if (hasBlocks) AppColors.Primary.copy(alpha = 0.5f) else AppColors.Border,
        borderWidth = if (hasBlocks) 1.dp else 0.5.dp,
        padding = 14.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
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
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = strings.mealTypeName(mealType),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                }

                // Trash Can Delete Icon Button on top right when slot has content
                if (hasContent) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
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
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (hasBlocks) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    slot.blocks.forEach { item ->
                        SlotBlockMiniBadge(item = item)
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppColors.SurfaceVariant.copy(alpha = 0.6f))
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.addMealBlockHint(strings.mealTypeName(mealType)),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.TextMuted
                    )
                }
            }

            if (hasMemo) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = strings.memoPrefix(slot.memo),
                    fontSize = 12.sp,
                    color = AppColors.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun SlotBlockMiniBadge(item: MealBlockItem) {
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier = Modifier
            .clip(shape)
            .background(AppColors.SurfaceVariant)
            .border(0.5.dp, AppColors.Border, shape)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        LegoTopViewBlock(
            colorHex = item.blockColorHex,
            moldCapacityMl = item.moldCapacityMl,
            width = 14.dp,
            height = 26.dp
        )

        Text(
            text = item.blockName,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary
        )
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
            val hasAnyBlocks = record != null && record.totalBlockCount > 0

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

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = strings.editArrow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.PrimaryDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (record != null && hasAnyBlocks) {
                        val allDayBlocks = remember(record) {
                            MealType.entries.flatMap { record.getSlot(it).blocks }
                        }
                        // Stacked pure top-view Lego blocks only
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            allDayBlocks.forEach { item ->
                                LegoTopViewBlock(
                                    colorHex = item.blockColorHex,
                                    moldCapacityMl = item.moldCapacityMl,
                                    width = 28.dp,
                                    height = 54.dp
                                )
                            }
                        }
                    } else {
                        Text(
                            text = strings.addMealPlanBtn,
                            fontSize = 12.sp,
                            color = AppColors.TextMuted
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
