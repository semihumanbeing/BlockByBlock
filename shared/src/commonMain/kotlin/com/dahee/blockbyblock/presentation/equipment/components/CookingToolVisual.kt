package com.dahee.blockbyblock.presentation.equipment.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import blockbyblock.shared.generated.resources.Res
import blockbyblock.shared.generated.resources.air_fryer
import blockbyblock.shared.generated.resources.blender
import blockbyblock.shared.generated.resources.gas_stove
import blockbyblock.shared.generated.resources.microwave
import blockbyblock.shared.generated.resources.other_utensils
import blockbyblock.shared.generated.resources.oven
import blockbyblock.shared.generated.resources.slow_cooker
import com.dahee.blockbyblock.domain.model.CookingToolType
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

fun getCookingToolDrawable(type: CookingToolType): DrawableResource {
    return when (type) {
        CookingToolType.GAS_STOVE -> Res.drawable.gas_stove
        CookingToolType.OVEN -> Res.drawable.oven
        CookingToolType.SLOW_COOKER -> Res.drawable.slow_cooker
        CookingToolType.BLENDER -> Res.drawable.blender
        CookingToolType.AIR_FRYER -> Res.drawable.air_fryer
        CookingToolType.MICROWAVE -> Res.drawable.microwave
    }
}

@Composable
fun CookingToolVisual(
    type: CookingToolType,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp
) {
    Image(
        painter = painterResource(getCookingToolDrawable(type)),
        contentDescription = type.displayName,
        contentScale = ContentScale.Fit,
        modifier = modifier.size(size)
    )
}
