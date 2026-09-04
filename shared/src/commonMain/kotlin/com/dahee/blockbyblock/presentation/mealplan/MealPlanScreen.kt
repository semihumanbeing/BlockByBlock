package com.dahee.blockbyblock.presentation.mealplan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dahee.blockbyblock.core.i18n.LocalStrings
import com.dahee.blockbyblock.core.theme.AppColors
import com.dahee.blockbyblock.core.ui.AppCard
import com.dahee.blockbyblock.core.ui.AppTextField
import com.dahee.blockbyblock.core.ui.AppToastBanner
import com.dahee.blockbyblock.domain.model.DayMealRecord
import com.dahee.blockbyblock.domain.model.MealSlotRecord
import com.dahee.blockbyblock.domain.model.MealType
import com.dahee.blockbyblock.presentation.mealplan.components.BentoLunchBoxView
import com.dahee.blockbyblock.presentation.mealplan.components.MealRecordDialog

@Composable
fun MealPlanScreen(
    viewModel: MealPlanViewModel,
    onCreateBlockClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    // Delete Confirmation Dialog State
    var pendingDeleteMealType by remember { mutableStateOf<MealType?>(null) }
    var pendingDeleteDateString by remember { mutableStateOf<String?>(null) }

    // Preset Save Dialog State
    var pendingSavePresetMealType by remember { mutableStateOf<MealType?>(null) }
    var presetNameInput by remember { mutableStateOf("") }

    // Meal Saved Transient Notice Toast State
    var showSavedNotice by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.onSlotSavedListener = {
            showSavedNotice = true
        }
    }

    LaunchedEffect(showSavedNotice) {
        if (showSavedNotice) {
            delay(2000)
            showSavedNotice = false
        }
    }

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
            onDismiss = { viewModel.onCloseSlotDialog() },
            onCreateBlockClick = {
                viewModel.onCloseSlotDialog()
                onCreateBlockClick?.invoke()
            }
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

    // Save Preset Dialog Modal
    if (pendingSavePresetMealType != null) {
        val targetMealType = pendingSavePresetMealType!!

        AlertDialog(
            onDismissRequest = {
                pendingSavePresetMealType = null
                presetNameInput = ""
            },
            title = {
                Text(
                    text = strings.saveMealPreset,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = strings.enterPresetName,
                        fontSize = 13.5.sp,
                        color = AppColors.TextSecondary
                    )

                    AppTextField(
                        value = presetNameInput,
                        onValueChange = { presetNameInput = it },
                        placeholder = strings.presetNamePlaceholder,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                viewModel.onSaveSlotAsPreset(
                                    dateString = uiState.selectedDateString,
                                    mealType = targetMealType,
                                    presetName = presetNameInput.trim().ifBlank { null }
                                )
                                pendingSavePresetMealType = null
                                presetNameInput = ""
                                showSavedNotice = true
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onSaveSlotAsPreset(
                            dateString = uiState.selectedDateString,
                            mealType = targetMealType,
                            presetName = presetNameInput.trim().ifBlank { null }
                        )
                        pendingSavePresetMealType = null
                        presetNameInput = ""
                        showSavedNotice = true
                    }
                ) {
                    Text(
                        text = strings.saveMealPreset,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Primary
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pendingSavePresetMealType = null
                        presetNameInput = ""
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                        val slot = uiState.currentDayMealRecord?.getSlot(mealType)
                        val defaultName = if (slot != null && slot.customTitle.isNotBlank()) {
                            slot.customTitle
                        } else if (slot != null && slot.blocks.isNotEmpty()) {
                            "${strings.mealTypeName(mealType)} (${slot.blocks.map { it.blockName }.distinct().joinToString(", ")})"
                        } else {
                            strings.mealTypeName(mealType)
                        }
                        presetNameInput = defaultName
                        pendingSavePresetMealType = mealType
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

        // Small Toast/Notice Badge: "식단이 저장되었습니다"
        AppToastBanner(
            visible = showSavedNotice,
            message = strings.presetSaved,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
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
    val scrollState = rememberScrollState()

    // 5 Meal Slots: Breakfast, Lunch, Dinner, Snack, Extra (up to 5 meals)
    val visibleSlots = remember(currentDayRecord) {
        val list = mutableListOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER)
        val snackRecord = currentDayRecord?.snack
        val hasSnack = snackRecord != null && (snackRecord.blocks.isNotEmpty() || snackRecord.memo.isNotBlank())
        list.add(MealType.SNACK)
        if (hasSnack) {
            list.add(MealType.EXTRA)
        }
        list
    }

    Column(
        modifier = Modifier.fillMaxSize()
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

        // Meal content area: fills remaining space between DateHeader and BottomNav
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val spacingDp = 8.dp
            val totalSpacing = spacingDp * (visibleSlots.size - 1)
            val availableHeight = maxHeight - totalSpacing

            // Count regular vs unrecorded-add slots for weighted distribution
            // Regular slot = 4 parts, unrecorded add slot = 1 part
            var regularCount = 0
            var addCount = 0
            visibleSlots.forEach { mealType ->
                val slotRecord = currentDayRecord?.getSlot(mealType) ?: MealSlotRecord(mealType)
                val isAddSlotType = mealType == MealType.SNACK || mealType == MealType.EXTRA
                val hasContent = slotRecord.blocks.isNotEmpty() || slotRecord.memo.isNotBlank()
                if (isAddSlotType && !hasContent) addCount++ else regularCount++
            }
            val totalParts = regularCount * 4 + addCount
            val onePart = if (totalParts > 0) availableHeight / totalParts else 0.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(spacingDp)
            ) {
                visibleSlots.forEach { mealType ->
                    val slotRecord = currentDayRecord?.getSlot(mealType) ?: MealSlotRecord(mealType)
                    val isAddSlotType = mealType == MealType.SNACK || mealType == MealType.EXTRA
                    val hasSlotContent = slotRecord.blocks.isNotEmpty() || slotRecord.memo.isNotBlank()
                    val isUnrecordedAddSlot = isAddSlotType && !hasSlotContent

                    val slotMinHeight = if (isUnrecordedAddSlot) onePart else onePart * 4

                    MealSlotCard(
                        mealType = mealType,
                        slot = slotRecord,
                        onClick = { onOpenSlot(mealType) },
                        onSavePresetClick = { onSavePresetSlot(mealType) },
                        onDeleteClick = { onPromptDeleteSlot(mealType) },
                        minCardHeight = slotMinHeight,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = slotMinHeight)
                    )
                }
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
    minCardHeight: Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val hasBlocks = slot.blocks.isNotEmpty()
    val hasMemo = slot.memo.isNotBlank()
    val hasContent = hasBlocks || hasMemo
    val isAddSlotType = mealType == MealType.SNACK || mealType == MealType.EXTRA
    val isUnrecordedAddSlot = isAddSlotType && !hasContent

    if (isUnrecordedAddSlot) {
        AppCard(
            modifier = modifier
                .fillMaxWidth()
                .pointerHoverIcon(PointerIcon.Hand)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            backgroundColor = AppColors.SurfaceVariant.copy(alpha = 0.25f),
            borderColor = AppColors.Border.copy(alpha = 0.6f),
            borderWidth = 1.dp,
            elevation = 0.dp,
            cornerRadius = 14.dp,
            padding = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = AppColors.TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = strings.addMealSlot,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextSecondary
                    )
                }
            }
        }
        return
    }

    val titleText = if (isAddSlotType && slot.customTitle.isNotBlank()) slot.customTitle else strings.mealTypeName(mealType)

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
        borderColor = AppColors.Border.copy(alpha = 0.45f),
        borderWidth = 0.5.dp,
        elevation = 0.dp,
        cornerRadius = 14.dp,
        padding = 10.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Header: Plain Black Meal Type Text + Save Preset + Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = titleText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )

                if (hasContent) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (hasBlocks) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(AppColors.PrimaryLight.copy(alpha = 0.45f))
                                    .border(1.dp, AppColors.Primary.copy(alpha = 0.55f), RoundedCornerShape(7.dp))
                                    .pointerHoverIcon(PointerIcon.Hand)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = onSavePresetClick
                                    )
                                    .padding(horizontal = 9.dp, vertical = 3.5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = strings.saveMealPreset,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.PrimaryDark
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .pointerHoverIcon(PointerIcon.Hand)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onDeleteClick
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = strings.delete,
                                tint = AppColors.TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // 2. Main Content: [Left 50%: Authentic Bento Box with Food Blocks] + [Right 50%: Block list line by line in order]
            // Calculate bento row height dynamically: card height minus header, padding, spacing
            val cardPaddingVertical = 10.dp * 2 // AppCard padding top + bottom
            val headerEstimatedHeight = 24.dp
            val spacingBetweenElements = 8.dp
            val memoEstimatedHeight = if (hasMemo) (16.dp + spacingBetweenElements) else 0.dp
            val bentoRowHeight = maxOf(
                84.dp,
                minCardHeight - cardPaddingVertical - headerEstimatedHeight - spacingBetweenElements - memoEstimatedHeight
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(bentoRowHeight),
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
                    onBlockClick = { onClick() },
                    emptyPlaceholder = {
                        Text(
                            text = "+",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextMuted.copy(alpha = 0.5f)
                        )
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
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    // Color Indicator (aligned with the first line of text)
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(AppColors.hexToColor(sampleBlock.blockColorHex))
                                            .border(0.5.dp, Color.Black.copy(alpha = 0.15f), CircleShape)
                                    )

                                    // Block Name (Wraps to line below when long)
                                    Text(
                                        text = sampleBlock.blockName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.TextPrimary,
                                        lineHeight = 15.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )

                                    // Quantity "× N개"
                                    Text(
                                        text = strings.blockCountSuffix(count),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppColors.TextSecondary,
                                        modifier = Modifier.padding(top = 1.dp)
                                    )
                                }
                            }
                        }
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

            // 3. Memo if present (Wraps naturally to multiline below the bento box)
            if (hasMemo) {
                Text(
                    text = strings.memoPrefix(slot.memo),
                    fontSize = 11.5.sp,
                    color = AppColors.TextSecondary,
                    lineHeight = 15.sp,
                    modifier = Modifier.fillMaxWidth()
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

    val mainMealTypes = listOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER)

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

        // 2. 7 Days Compact Bento Rows
        weekDays.forEach { dayModel ->
            val record = dayModel.mealRecord
            val isToday = dayModel.isToday
            val hasBlocksInDay = record != null && (
                record.breakfast.blocks.isNotEmpty() ||
                record.lunch.blocks.isNotEmpty() ||
                record.dinner.blocks.isNotEmpty() ||
                record.snack.blocks.isNotEmpty() ||
                record.extra.blocks.isNotEmpty()
            )

            val extraMealTypes = listOf(MealType.SNACK, MealType.EXTRA).filter { type ->
                val slot = record?.getSlot(type)
                slot != null && (slot.blocks.isNotEmpty() || slot.memo.isNotBlank())
            }
            val hasExtra = extraMealTypes.isNotEmpty()

            AppCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(if (hasExtra) 1.45f else 1f)
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
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Left: Date Badge (요일 + 월/일)
                    Column(
                        modifier = Modifier
                            .width(46.dp)
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
                                dayModel.isSunday -> Color(0xFFD32F2F)
                                dayModel.isSaturday -> Color(0xFF1976D2)
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

                    // Right: Authentic Bento Slots (Line 1: Main 3 meals / Line 2: Extra meals if recorded)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Line 1: Main 3 meals (아침, 점심, 저녁 - 3 Bento Boxes)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            mainMealTypes.forEach { mealType ->
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

                        // Line 2 (Only if 4th/5th meals exist): Extra meals shown on next line
                        if (hasExtra) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(0.9f),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                extraMealTypes.forEach { mealType ->
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
                                if (extraMealTypes.size == 1) {
                                    Spacer(modifier = Modifier.weight(2f))
                                } else if (extraMealTypes.size == 2) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "View Day",
                        tint = if (isToday) AppColors.Primary else AppColors.TextMuted.copy(alpha = 0.4f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
    }
}
