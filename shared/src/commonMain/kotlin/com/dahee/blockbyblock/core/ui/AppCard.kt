package com.dahee.blockbyblock.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dahee.blockbyblock.core.theme.AppColors

import androidx.compose.ui.draw.shadow

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = AppColors.Surface,
    borderColor: Color = AppColors.Border.copy(alpha = 0.6f),
    borderWidth: Dp = 0.5.dp,
    elevation: Dp = 2.dp,
    cornerRadius: Dp = 16.dp,
    padding: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val baseModifier = modifier
        .shadow(
            elevation = elevation,
            shape = shape,
            clip = false,
            spotColor = AppColors.Shadow,
            ambientColor = AppColors.Shadow
        )
        .clip(shape)
        .background(backgroundColor)
        .then(
            if (borderWidth > 0.dp) {
                Modifier.border(borderWidth, borderColor, shape)
            } else Modifier
        )

    val cardModifier = if (onClick != null) {
        baseModifier
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(padding)
    } else {
        baseModifier.padding(padding)
    }

    Column(
        modifier = cardModifier,
        content = content
    )
}
