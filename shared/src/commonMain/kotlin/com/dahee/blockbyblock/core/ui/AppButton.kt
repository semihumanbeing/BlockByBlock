package com.dahee.blockbyblock.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dahee.blockbyblock.core.theme.AppColors

enum class ButtonVariant {
    PRIMARY,
    SECONDARY,
    OUTLINE,
    DANGER
}

@Composable
fun AppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.PRIMARY,
    enabled: Boolean = true,
    height: Dp = 48.dp,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val (bgColor, textColor, borderColor) = when (variant) {
        ButtonVariant.PRIMARY -> listOf(
            if (enabled) AppColors.Primary else AppColors.Border,
            if (enabled) Color.White else AppColors.TextMuted,
            if (enabled) AppColors.PrimaryDark else AppColors.Border
        )
        ButtonVariant.SECONDARY -> listOf(
            if (enabled) AppColors.SurfaceVariant else AppColors.Border,
            if (enabled) AppColors.TextPrimary else AppColors.TextMuted,
            if (enabled) AppColors.Border else AppColors.Border
        )
        ButtonVariant.OUTLINE -> listOf(
            Color.Transparent,
            if (enabled) AppColors.Primary else AppColors.TextMuted,
            if (enabled) AppColors.Primary else AppColors.Border
        )
        ButtonVariant.DANGER -> listOf(
            if (enabled) AppColors.Danger else AppColors.Border,
            if (enabled) Color.White else AppColors.TextMuted,
            if (enabled) Color(0xFFB03A2E) else AppColors.Border
        )
    }

    val shape = RoundedCornerShape(14.dp)
    val pressOffsetY = if (isPressed && enabled) 2.dp else 0.dp
    val elevation = if (isPressed || !enabled) 0.dp else if (variant == ButtonVariant.PRIMARY) 3.dp else 1.dp

    Box(
        modifier = modifier
            .height(height)
            .offset(y = pressOffsetY)
            .shadow(
                elevation = elevation,
                shape = shape,
                clip = false,
                spotColor = AppColors.Shadow,
                ambientColor = AppColors.Shadow
            )
            .clip(shape)
            .background(bgColor)
            .border(1.dp, borderColor, shape)
            .pointerHoverIcon(if (enabled) PointerIcon.Hand else PointerIcon.Default)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides textColor) {
            ProvideTextStyle(TextStyle(color = textColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    content = content
                )
            }
        }
    }
}

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.PRIMARY,
    enabled: Boolean = true,
    height: Dp = 48.dp
) {
    val textColor = when (variant) {
        ButtonVariant.PRIMARY -> if (enabled) Color.White else AppColors.TextMuted
        ButtonVariant.SECONDARY -> if (enabled) AppColors.TextPrimary else AppColors.TextMuted
        ButtonVariant.OUTLINE -> if (enabled) AppColors.Primary else AppColors.TextMuted
        ButtonVariant.DANGER -> if (enabled) Color.White else AppColors.TextMuted
    }

    AppButton(
        onClick = onClick,
        modifier = modifier,
        variant = variant,
        enabled = enabled,
        height = height
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
