package com.dahee.blockbyblock.presentation.mealplan.components

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.dahee.blockbyblock.core.i18n.AppLanguage
import com.dahee.blockbyblock.core.i18n.LocalStrings
import com.dahee.blockbyblock.core.theme.AppColors
import com.dahee.blockbyblock.core.ui.AppButton
import com.dahee.blockbyblock.core.ui.ButtonVariant
import com.dahee.blockbyblock.presentation.mealplan.MealPlanViewModel

/**
 * Calendar Date Picker Dialog for daily meal plan date navigation.
 */
@Composable
fun MealPlanDatePickerDialog(
    initialDate: String,
    todayDate: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current
    val initialParts = initialDate.split("-")
    val initialYear = initialParts.getOrNull(0)?.toIntOrNull() ?: 2026
    val initialMonth = initialParts.getOrNull(1)?.toIntOrNull() ?: 9

    var displayYear by remember { mutableStateOf(initialYear) }
    var displayMonth by remember { mutableStateOf(initialMonth) }

    fun prevMonth() {
        if (displayMonth == 1) {
            displayMonth = 12
            displayYear -= 1
        } else {
            displayMonth -= 1
        }
    }

    fun nextMonth() {
        if (displayMonth == 12) {
            displayMonth = 1
            displayYear += 1
        } else {
            displayMonth += 1
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(AppColors.Surface)
                .border(1.dp, AppColors.Border, RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Month Header: Prev / Month & Year Title / Next
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AppColors.SurfaceVariant.copy(alpha = 0.8f))
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { prevMonth() }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Month",
                            tint = AppColors.TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = "${displayYear}년 ${displayMonth}월",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AppColors.SurfaceVariant.copy(alpha = 0.8f))
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { nextMonth() }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Month",
                            tint = AppColors.TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // 2. Day of Week Header Row (Monday to Sunday)
                val dayHeaders = listOf("월", "화", "수", "목", "금", "토", "일")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dayHeaders.forEachIndexed { index, name ->
                        val textColor = when (index) {
                            5 -> Color(0xFF2563EB) // Saturday: Blue
                            6 -> Color(0xFFEF4444) // Sunday: Red
                            else -> AppColors.TextSecondary
                        }
                        Text(
                            text = name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // 3. Calendar Grid
                val firstDayIso = "$displayYear-${if (displayMonth < 10) "0$displayMonth" else "$displayMonth"}-01"
                val firstDayDow = MealPlanViewModel.getDayOfWeekNumber(firstDayIso)
                val leadingEmpty = if (firstDayDow == 0) 6 else firstDayDow - 1
                val daysInCurrentMonth = MealPlanViewModel.daysInMonth(displayYear, displayMonth)

                val prevMonthNum = if (displayMonth == 1) 12 else displayMonth - 1
                val prevYearNum = if (displayMonth == 1) displayYear - 1 else displayYear
                val daysInPrevMonth = MealPlanViewModel.daysInMonth(prevYearNum, prevMonthNum)

                val nextMonthNum = if (displayMonth == 12) 1 else displayMonth + 1
                val nextYearNum = if (displayMonth == 12) displayYear + 1 else displayYear

                val totalCells = leadingEmpty + daysInCurrentMonth
                val rowCount = (totalCells + 6) / 7

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (row in 0 until rowCount) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (col in 0 until 7) {
                                val cellIdx = row * 7 + col
                                when {
                                    cellIdx < leadingEmpty -> {
                                        // Previous month trailing days
                                        val prevDay = daysInPrevMonth - leadingEmpty + 1 + cellIdx
                                        val prevDateIso = "$prevYearNum-${if (prevMonthNum < 10) "0$prevMonthNum" else "$prevMonthNum"}-${if (prevDay < 10) "0$prevDay" else "$prevDay"}"
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(38.dp)
                                                .pointerHoverIcon(PointerIcon.Hand)
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null,
                                                    onClick = {
                                                        onDateSelected(prevDateIso)
                                                        onDismiss()
                                                    }
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "$prevDay",
                                                fontSize = 13.sp,
                                                color = AppColors.TextMuted.copy(alpha = 0.35f)
                                            )
                                        }
                                    }
                                    cellIdx < leadingEmpty + daysInCurrentMonth -> {
                                        // Current month days
                                        val day = cellIdx - leadingEmpty + 1
                                        val dateIso = "$displayYear-${if (displayMonth < 10) "0$displayMonth" else "$displayMonth"}-${if (day < 10) "0$day" else "$day"}"
                                        val isSelected = dateIso == initialDate
                                        val isToday = dateIso == todayDate

                                        val textColor = when {
                                            isSelected -> Color.White
                                            isToday -> AppColors.PrimaryDark
                                            col == 5 -> Color(0xFF2563EB) // Saturday
                                            col == 6 -> Color(0xFFEF4444) // Sunday
                                            else -> AppColors.TextPrimary
                                        }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(38.dp)
                                                .pointerHoverIcon(PointerIcon.Hand)
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null,
                                                    onClick = {
                                                        onDateSelected(dateIso)
                                                        onDismiss()
                                                    }
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        when {
                                                            isSelected -> AppColors.Primary
                                                            isToday -> AppColors.PrimaryLight.copy(alpha = 0.4f)
                                                            else -> Color.Transparent
                                                        }
                                                    )
                                                    .border(
                                                        width = when {
                                                            isSelected -> 0.dp
                                                            isToday -> 1.5.dp
                                                            else -> 0.dp
                                                        },
                                                        color = if (isToday) AppColors.Primary else Color.Transparent,
                                                        shape = CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "$day",
                                                    fontSize = 13.5.sp,
                                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                    color = textColor
                                                )
                                            }
                                        }
                                    }
                                    else -> {
                                        // Next month leading days
                                        val nextDay = cellIdx - (leadingEmpty + daysInCurrentMonth) + 1
                                        val nextDateIso = "$nextYearNum-${if (nextMonthNum < 10) "0$nextMonthNum" else "$nextMonthNum"}-${if (nextDay < 10) "0$nextDay" else "$nextDay"}"
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(38.dp)
                                                .pointerHoverIcon(PointerIcon.Hand)
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null,
                                                    onClick = {
                                                        onDateSelected(nextDateIso)
                                                        onDismiss()
                                                    }
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "$nextDay",
                                                fontSize = 13.sp,
                                                color = AppColors.TextMuted.copy(alpha = 0.35f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // 4. Bottom Action Bar: [오늘] shortcut & [닫기]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppButton(
                        text = strings.today,
                        variant = ButtonVariant.SECONDARY,
                        onClick = {
                            onDateSelected(todayDate)
                            onDismiss()
                        },
                        height = 38.dp
                    )

                    AppButton(
                        text = strings.close,
                        variant = ButtonVariant.SECONDARY,
                        onClick = onDismiss,
                        height = 38.dp
                    )
                }
            }
        }
    }
}

/**
 * Calendar Week Picker Dialog for weekly meal plan week navigation.
 */
@Composable
fun MealPlanWeekPickerDialog(
    currentWeekStartDate: String,
    todayDate: String,
    onWeekSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current
    val initialParts = currentWeekStartDate.split("-")
    val initialYear = initialParts.getOrNull(0)?.toIntOrNull() ?: 2026
    val initialMonth = initialParts.getOrNull(1)?.toIntOrNull() ?: 9

    var displayYear by remember { mutableStateOf(initialYear) }
    var displayMonth by remember { mutableStateOf(initialMonth) }

    fun prevMonth() {
        if (displayMonth == 1) {
            displayMonth = 12
            displayYear -= 1
        } else {
            displayMonth -= 1
        }
    }

    fun nextMonth() {
        if (displayMonth == 12) {
            displayMonth = 1
            displayYear += 1
        } else {
            displayMonth += 1
        }
    }

    // Compute weeks intersecting display month
    val firstDayIso = "$displayYear-${if (displayMonth < 10) "0$displayMonth" else "$displayMonth"}-01"
    val daysInCurMonth = MealPlanViewModel.daysInMonth(displayYear, displayMonth)
    val lastDayIso = "$displayYear-${if (displayMonth < 10) "0$displayMonth" else "$displayMonth"}-${if (daysInCurMonth < 10) "0$daysInCurMonth" else "$daysInCurMonth"}"
    val firstMonday = MealPlanViewModel.getMondayOfWeek(firstDayIso)
    val lastMonday = MealPlanViewModel.getMondayOfWeek(lastDayIso)

    val weeksInMonth = remember(displayYear, displayMonth) {
        val list = mutableListOf<String>()
        var cur = firstMonday
        while (cur <= lastMonday) {
            list.add(cur)
            cur = MealPlanViewModel.offsetDate(cur, 7)
        }
        list
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(AppColors.Surface)
                .border(1.dp, AppColors.Border, RoundedCornerShape(20.dp))
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Month Header: Prev / Month & Year Title / Next
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AppColors.SurfaceVariant.copy(alpha = 0.8f))
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { prevMonth() }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Month",
                            tint = AppColors.TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = "${displayYear}년 ${displayMonth}월",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AppColors.SurfaceVariant.copy(alpha = 0.8f))
                            .pointerHoverIcon(PointerIcon.Hand)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { nextMonth() }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Month",
                            tint = AppColors.TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // 2. Weekdays Header Row: [주차] + 월 화 수 목 금 토 일
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = strings.selectWeek,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextMuted,
                        modifier = Modifier.width(44.dp),
                        textAlign = TextAlign.Center
                    )

                    val dayHeaders = listOf("월", "화", "수", "목", "금", "토", "일")
                    dayHeaders.forEachIndexed { index, name ->
                        val textColor = when (index) {
                            5 -> Color(0xFF2563EB)
                            6 -> Color(0xFFEF4444)
                            else -> AppColors.TextSecondary
                        }
                        Text(
                            text = name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // 3. Week Rows List
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    weeksInMonth.forEachIndexed { weekIndex, mondayStr ->
                        val weekDays = remember(mondayStr) {
                            (0..6).map { MealPlanViewModel.offsetDate(mondayStr, it) }
                        }
                        val isSelectedWeek = mondayStr == currentWeekStartDate
                        val isCurrentWeek = weekDays.contains(todayDate)

                        val startParts = mondayStr.split("-")
                        val sm = startParts[1].toIntOrNull() ?: 1
                        val sd = startParts[2].toIntOrNull() ?: 1
                        val endParts = weekDays[6].split("-")
                        val em = endParts[1].toIntOrNull() ?: 1
                        val ed = endParts[2].toIntOrNull() ?: 1

                        val weekLabel = "${weekIndex + 1}주차"

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when {
                                        isSelectedWeek -> AppColors.PrimaryLight.copy(alpha = 0.55f)
                                        isCurrentWeek -> AppColors.SurfaceVariant.copy(alpha = 0.5f)
                                        else -> Color.Transparent
                                    }
                                )
                                .border(
                                    width = if (isSelectedWeek) 1.5.dp else if (isCurrentWeek) 1.dp else 0.5.dp,
                                    color = when {
                                        isSelectedWeek -> AppColors.Primary
                                        isCurrentWeek -> AppColors.Primary.copy(alpha = 0.5f)
                                        else -> AppColors.Border.copy(alpha = 0.4f)
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .pointerHoverIcon(PointerIcon.Hand)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        onWeekSelected(mondayStr)
                                        onDismiss()
                                    }
                                )
                                .padding(horizontal = 6.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Week Badge
                                Box(
                                    modifier = Modifier
                                        .width(44.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (isSelectedWeek) AppColors.Primary else AppColors.SurfaceVariant
                                        )
                                        .padding(vertical = 3.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = weekLabel,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelectedWeek) Color.White else AppColors.TextPrimary
                                    )
                                }

                                // 7 Days in Week
                                weekDays.forEachIndexed { colIdx, dayDateStr ->
                                    val parts = dayDateStr.split("-")
                                    val m = parts.getOrNull(1)?.toIntOrNull() ?: 1
                                    val d = parts.getOrNull(2)?.toIntOrNull() ?: 1
                                    val isCurrentMonthDay = m == displayMonth
                                    val isDayToday = dayDateStr == todayDate

                                    val textColor = when {
                                        !isCurrentMonthDay -> AppColors.TextMuted.copy(alpha = 0.35f)
                                        isDayToday -> AppColors.PrimaryDark
                                        colIdx == 5 -> Color(0xFF2563EB)
                                        colIdx == 6 -> Color(0xFFEF4444)
                                        else -> AppColors.TextPrimary
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(26.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isDayToday) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(AppColors.PrimaryLight.copy(alpha = 0.45f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "$d",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = textColor
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = "$d",
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelectedWeek) FontWeight.SemiBold else FontWeight.Normal,
                                                color = textColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // 4. Bottom Action Bar: [이번 주] & [닫기]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val currentWeekMonday = MealPlanViewModel.getMondayOfWeek(todayDate)
                    AppButton(
                        text = strings.thisWeek,
                        variant = ButtonVariant.SECONDARY,
                        onClick = {
                            onWeekSelected(currentWeekMonday)
                            onDismiss()
                        },
                        height = 38.dp
                    )

                    AppButton(
                        text = strings.close,
                        variant = ButtonVariant.SECONDARY,
                        onClick = onDismiss,
                        height = 38.dp
                    )
                }
            }
        }
    }
}
