package com.dahee.blockbyblock.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import kotlinx.coroutines.delay

/**
 * Stepper component supporting direct number input and arrow buttons.
 * Automatically exits edit mode and commits value when tapping outside or losing focus.
 */
@Composable
fun EditableNumberStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    suffix: String = "",
    step: Int = 1,
    minValue: Int = 1,
    maxValue: Int = 99999,
    buttonSize: Dp = 22.dp,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    var textInput by remember(value) { mutableStateOf(TextFieldValue(value.toString(), selection = TextRange(value.toString().length))) }
    var hasFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(isEditing) {
        if (isEditing) {
            textInput = TextFieldValue(value.toString(), selection = TextRange(value.toString().length))
            hasFocused = false
            delay(30)
            try {
                focusRequester.requestFocus()
            } catch (_: Throwable) {
                // Focus requester may throw if not attached yet
            }
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(AppColors.SurfaceVariant)
            .border(1.dp, AppColors.Border, RoundedCornerShape(8.dp))
            .padding(horizontal = 2.dp, vertical = 2.dp)
    ) {
        // Decrement button (▼)
        Box(
            modifier = Modifier
                .size(buttonSize)
                .clip(RoundedCornerShape(4.dp))
                .pointerHoverIcon(PointerIcon.Hand)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        if (isEditing) {
                            val parsed = textInput.text.toIntOrNull() ?: value
                            onValueChange(parsed.coerceIn(minValue, maxValue))
                            isEditing = false
                            hasFocused = false
                        }
                        val newValue = (value - step).coerceAtLeast(minValue)
                        onValueChange(newValue)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Decrease",
                tint = if (value > minValue) AppColors.TextPrimary else AppColors.TextMuted,
                modifier = Modifier.size(16.dp)
            )
        }

        // Center text / direct input field
        if (isEditing) {
            BasicTextField(
                value = textInput,
                onValueChange = { input ->
                    if (input.text.all { it.isDigit() } && input.text.length <= 5) {
                        textInput = input
                        val parsed = input.text.toIntOrNull()
                        if (parsed != null && parsed >= minValue && parsed <= maxValue) {
                            onValueChange(parsed)
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        val parsed = textInput.text.toIntOrNull() ?: value
                        onValueChange(parsed.coerceIn(minValue, maxValue))
                        isEditing = false
                        hasFocused = false
                        focusManager.clearFocus()
                    }
                ),
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryDark,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .defaultMinSize(minWidth = 38.dp)
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            hasFocused = true
                        } else if (hasFocused && isEditing) {
                            val parsed = textInput.text.toIntOrNull() ?: value
                            onValueChange(parsed.coerceIn(minValue, maxValue))
                            isEditing = false
                            hasFocused = false
                        }
                    }
                    .background(Color.White, RoundedCornerShape(4.dp))
                    .border(1.dp, AppColors.Primary, RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            )
        } else {
            Text(
                text = "$value$suffix",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
                modifier = Modifier
                    .defaultMinSize(minWidth = 32.dp)
                    .pointerHoverIcon(PointerIcon.Hand)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            isEditing = true
                        }
                    )
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        // Increment button (▲)
        Box(
            modifier = Modifier
                .size(buttonSize)
                .clip(RoundedCornerShape(4.dp))
                .pointerHoverIcon(PointerIcon.Hand)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        if (isEditing) {
                            val parsed = textInput.text.toIntOrNull() ?: value
                            onValueChange(parsed.coerceIn(minValue, maxValue))
                            isEditing = false
                            hasFocused = false
                        }
                        val newValue = (value + step).coerceAtMost(maxValue)
                        onValueChange(newValue)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Increase",
                tint = AppColors.Primary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
