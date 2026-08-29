package com.dahee.blockbyblock.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dahee.blockbyblock.core.i18n.LocalStrings
import com.dahee.blockbyblock.core.theme.AppColors

/**
 * Chip component that shows 'Custom' and switches to an inline number input on tap.
 */
@Composable
fun CustomSlotChip(
    currentCellCount: Int,
    presetList: List<Int>,
    onCellCountChange: (Int) -> Unit,
    minCellCount: Int = 1,
    maxCellCount: Int = 999,
    horizontalPadding: Dp = 8.dp,
    verticalPadding: Dp = 4.dp,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    var isEditing by remember { mutableStateOf(false) }
    var textValue by remember(currentCellCount, isEditing) {
        mutableStateOf(
            TextFieldValue(
                text = currentCellCount.toString(),
                selection = TextRange(currentCellCount.toString().length)
            )
        )
    }
    val focusRequester = remember { FocusRequester() }

    val isCustomActive = !presetList.contains(currentCellCount)
    val shape = RoundedCornerShape(8.dp)

    if (isEditing) {
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .clip(shape)
                .background(AppColors.SurfaceVariant)
                .border(1.5.dp, AppColors.Primary, shape)
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            BasicTextField(
                value = textValue,
                onValueChange = { newValue ->
                    if (newValue.text.all { it.isDigit() } && newValue.text.length <= 3) {
                        textValue = newValue
                        val parsed = newValue.text.toIntOrNull()
                        if (parsed != null && parsed >= minCellCount && parsed <= maxCellCount) {
                            onCellCountChange(parsed)
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        val parsed = textValue.text.toIntOrNull() ?: currentCellCount
                        onCellCountChange(parsed.coerceIn(minCellCount, maxCellCount))
                        isEditing = false
                    }
                ),
                singleLine = true,
                cursorBrush = SolidColor(AppColors.Primary),
                textStyle = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryDark,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .width(36.dp)
                    .focusRequester(focusRequester)
            )

            Text(
                text = strings.unitSlot,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextSecondary
            )

            Spacer(modifier = Modifier.width(3.dp))

            // Checkmark button to confirm input
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(AppColors.Primary)
                    .pointerHoverIcon(PointerIcon.Hand)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            val parsed = textValue.text.toIntOrNull() ?: currentCellCount
                            onCellCountChange(parsed.coerceIn(minCellCount, maxCellCount))
                            isEditing = false
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = strings.done,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    } else {
        val displayText = if (isCustomActive) {
            strings.slotCount(currentCellCount)
        } else {
            strings.customSlotBtn
        }

        val bgColor = if (isCustomActive) AppColors.Primary else AppColors.SurfaceVariant
        val textColor = if (isCustomActive) Color.White else AppColors.TextSecondary
        val borderColor = if (isCustomActive) AppColors.PrimaryDark else AppColors.Border

        Box(
            modifier = modifier
                .clip(shape)
                .background(bgColor)
                .border(1.dp, borderColor, shape)
                .pointerHoverIcon(PointerIcon.Hand)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { isEditing = true }
                )
                .padding(horizontal = horizontalPadding, vertical = verticalPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayText,
                fontSize = 12.sp,
                fontWeight = if (isCustomActive) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
