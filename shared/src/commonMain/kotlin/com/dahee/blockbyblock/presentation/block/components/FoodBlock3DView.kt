package com.dahee.blockbyblock.presentation.block.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import blockbyblock.shared.generated.resources.Res
import blockbyblock.shared.generated.resources.food_block_3d_1x4_brown
import blockbyblock.shared.generated.resources.food_block_3d_1x4_green
import blockbyblock.shared.generated.resources.food_block_3d_1x4_orange
import blockbyblock.shared.generated.resources.food_block_3d_1x4_red
import blockbyblock.shared.generated.resources.food_block_3d_1x4_white
import blockbyblock.shared.generated.resources.food_block_3d_1x4_yellow
import blockbyblock.shared.generated.resources.food_block_3d_2x2_brown
import blockbyblock.shared.generated.resources.food_block_3d_2x2_green
import blockbyblock.shared.generated.resources.food_block_3d_2x2_orange
import blockbyblock.shared.generated.resources.food_block_3d_2x2_red
import blockbyblock.shared.generated.resources.food_block_3d_2x2_white
import blockbyblock.shared.generated.resources.food_block_3d_2x2_yellow
import blockbyblock.shared.generated.resources.food_block_3d_2x4_brown
import blockbyblock.shared.generated.resources.food_block_3d_2x4_green
import blockbyblock.shared.generated.resources.food_block_3d_2x4_orange
import blockbyblock.shared.generated.resources.food_block_3d_2x4_red
import blockbyblock.shared.generated.resources.food_block_3d_2x4_white
import blockbyblock.shared.generated.resources.food_block_3d_2x4_yellow
import blockbyblock.shared.generated.resources.food_block_3d_3x4_brown
import blockbyblock.shared.generated.resources.food_block_3d_3x4_green
import blockbyblock.shared.generated.resources.food_block_3d_3x4_orange
import blockbyblock.shared.generated.resources.food_block_3d_3x4_red
import blockbyblock.shared.generated.resources.food_block_3d_3x4_white
import blockbyblock.shared.generated.resources.food_block_3d_3x4_yellow
import com.dahee.blockbyblock.domain.model.BlockSizeCategory
import com.dahee.blockbyblock.domain.model.FoodBlockColorType
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Returns the exact 3D food block drawable based on mold capacity size category:
 * - MINI: 2x2 (0 ~ 100ml)
 * - SMALL: 1x4 (101 ~ 200ml)
 * - MEDIUM: 2x4 (201 ~ 399ml)
 * - LARGE: 3x4 (400ml ~)
 */
fun getFoodBlock3DDrawable(colorHex: String, moldCapacityMl: Int = 250): DrawableResource {
    val colorType = FoodBlockColorType.fromHex(colorHex)
    val category = BlockSizeCategory.fromCapacity(moldCapacityMl)

    return when (category) {
        BlockSizeCategory.LARGE -> when (colorType) {
            FoodBlockColorType.RED -> Res.drawable.food_block_3d_3x4_red
            FoodBlockColorType.YELLOW -> Res.drawable.food_block_3d_3x4_yellow
            FoodBlockColorType.GREEN -> Res.drawable.food_block_3d_3x4_green
            FoodBlockColorType.BROWN -> Res.drawable.food_block_3d_3x4_brown
            FoodBlockColorType.WHITE -> Res.drawable.food_block_3d_3x4_white
            FoodBlockColorType.ORANGE -> Res.drawable.food_block_3d_3x4_orange
        }
        BlockSizeCategory.MEDIUM -> when (colorType) {
            FoodBlockColorType.RED -> Res.drawable.food_block_3d_2x4_red
            FoodBlockColorType.YELLOW -> Res.drawable.food_block_3d_2x4_yellow
            FoodBlockColorType.GREEN -> Res.drawable.food_block_3d_2x4_green
            FoodBlockColorType.BROWN -> Res.drawable.food_block_3d_2x4_brown
            FoodBlockColorType.WHITE -> Res.drawable.food_block_3d_2x4_white
            FoodBlockColorType.ORANGE -> Res.drawable.food_block_3d_2x4_orange
        }
        BlockSizeCategory.SMALL -> when (colorType) {
            FoodBlockColorType.RED -> Res.drawable.food_block_3d_1x4_red
            FoodBlockColorType.YELLOW -> Res.drawable.food_block_3d_1x4_yellow
            FoodBlockColorType.GREEN -> Res.drawable.food_block_3d_1x4_green
            FoodBlockColorType.BROWN -> Res.drawable.food_block_3d_1x4_brown
            FoodBlockColorType.WHITE -> Res.drawable.food_block_3d_1x4_white
            FoodBlockColorType.ORANGE -> Res.drawable.food_block_3d_1x4_orange
        }
        BlockSizeCategory.MINI -> when (colorType) {
            FoodBlockColorType.RED -> Res.drawable.food_block_3d_2x2_red
            FoodBlockColorType.YELLOW -> Res.drawable.food_block_3d_2x2_yellow
            FoodBlockColorType.GREEN -> Res.drawable.food_block_3d_2x2_green
            FoodBlockColorType.BROWN -> Res.drawable.food_block_3d_2x2_brown
            FoodBlockColorType.WHITE -> Res.drawable.food_block_3d_2x2_white
            FoodBlockColorType.ORANGE -> Res.drawable.food_block_3d_2x2_orange
        }
    }
}

@Composable
fun FoodBlock3DView(
    colorHex: String,
    modifier: Modifier = Modifier,
    moldCapacityMl: Int = 250,
    size: Dp = 120.dp
) {
    val drawableRes = getFoodBlock3DDrawable(colorHex = colorHex, moldCapacityMl = moldCapacityMl)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(drawableRes),
            contentDescription = "3D Food Block",
            modifier = Modifier.size(size)
        )
    }
}
