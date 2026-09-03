package com.dahee.blockbyblock.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalFocusManager
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
import com.dahee.blockbyblock.core.theme.AppColors
import com.dahee.blockbyblock.core.utils.MoldCapacityFormatter
import com.dahee.blockbyblock.domain.model.MoldCapacityUnit
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * Capacity Stepper matching the exact visual style, sizing and borders of EditableNumberStepper.
 * Dynamically supports both ML and CUP/TBSP units, stepping and direct numeric input.
 */
@Composable
fun EditableCapacityStepper(
    capacityMl: Int,
    unit: MoldCapacityUnit,
    onCapacityChange: (Int) -> Unit,
    minCapacityMl: Int = 10,
    maxCapacityMl: Int = 2000,
    buttonSize: Dp = 26.dp,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var isEditing by remember { mutableStateOf(false) }
    var hasFocused by remember { mutableStateOf(false) }

    fun computeRawText(): String {
        return if (unit == MoldCapacityUnit.ML) {
            capacityMl.toString()
        } else {
            if (capacityMl >= 125) {
                val cups = (capacityMl / 250.0 * 10).roundToInt() / 10.0
                if (cups % 1.0 == 0.0) cups.toInt().toString() else cups.toString()
            } else {
                ((capacityMl / 15.0).roundToInt().coerceAtLeast(1)).toString()
            }
        }
    }

    var textInput by remember(capacityMl, unit, isEditing) {
        val raw = computeRawText()
        mutableStateOf(TextFieldValue(raw, selection = TextRange(raw.length)))
    }

    LaunchedEffect(isEditing) {
        if (isEditing) {
            val raw = computeRawText()
            textInput = TextFieldValue(raw, selection = TextRange(raw.length))
            hasFocused = false
            delay(30)
            try {
                focusRequester.requestFocus()
            } catch (_: Throwable) {}
        }
    }

    fun parseAndCommit(text: String) {
        val num = text.toDoubleOrNull()
        if (num != null && num > 0) {
            val computedMl = if (unit == MoldCapacityUnit.ML) {
                num.toInt()
            } else if (capacityMl >= 125 || num >= 0.5) {
                (num * 250.0).roundToInt()
            } else {
                (num * 15.0).roundToInt()
            }
            onCapacityChange(computedMl.coerceIn(minCapacityMl, maxCapacityMl))
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .width(114.dp)
            .height(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.SurfaceVariant)
            .border(1.dp, AppColors.Border, RoundedCornerShape(8.dp))
            .padding(horizontal = 3.dp, vertical = 2.dp)
    ) {
        // Decrement button (-)
        Box(
            modifier = Modifier
                .size(buttonSize)
                .clip(RoundedCornerShape(6.dp))
                .pointerHoverIcon(PointerIcon.Hand)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        if (isEditing) {
                            parseAndCommit(textInput.text)
                            isEditing = false
                            hasFocused = false
                        }
                        val next = MoldCapacityFormatter.stepCapacity(capacityMl, -1, unit)
                        onCapacityChange(next.coerceIn(minCapacityMl, maxCapacityMl))
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Decrease Capacity",
                tint = if (capacityMl > minCapacityMl) AppColors.TextPrimary else AppColors.TextMuted,
                modifier = Modifier.size(15.dp)
            )
        }

        // Center formatted text / direct input field
        if (isEditing) {
            BasicTextField(
                value = textInput,
                onValueChange = { input ->
                    textInput = input
                    parseAndCommit(input.text)
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (unit == MoldCapacityUnit.ML) KeyboardType.Number else KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        parseAndCommit(textInput.text)
                        isEditing = false
                        hasFocused = false
                        focusManager.clearFocus()
                    }
                ),
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryDark,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            hasFocused = true
                        } else if (hasFocused && isEditing) {
                            parseAndCommit(textInput.text)
                            isEditing = false
                            hasFocused = false
                        }
                    }
                    .background(Color.White, RoundedCornerShape(4.dp))
                    .border(1.dp, AppColors.Primary, RoundedCornerShape(4.dp))
                    .padding(horizontal = 2.dp, vertical = 1.dp)
            )
        } else {
            val formatted = MoldCapacityFormatter.formatCapacity(capacityMl, unit)
            Text(
                text = formatted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
                modifier = Modifier
                    .weight(1f)
                    .pointerHoverIcon(PointerIcon.Hand)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { isEditing = true }
                    )
                    .padding(horizontal = 2.dp, vertical = 1.dp)
            )
        }

        // Increment button (+)
        Box(
            modifier = Modifier
                .size(buttonSize)
                .clip(RoundedCornerShape(6.dp))
                .pointerHoverIcon(PointerIcon.Hand)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        if (isEditing) {
                            parseAndCommit(textInput.text)
                            isEditing = false
                            hasFocused = false
                        }
                        val next = MoldCapacityFormatter.stepCapacity(capacityMl, 1, unit)
                        onCapacityChange(next.coerceIn(minCapacityMl, maxCapacityMl))
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Increase Capacity",
                tint = AppColors.Primary,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}
