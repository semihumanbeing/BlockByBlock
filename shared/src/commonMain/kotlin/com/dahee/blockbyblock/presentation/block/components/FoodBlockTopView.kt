package com.dahee.blockbyblock.presentation.block.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import blockbyblock.shared.generated.resources.Res
import blockbyblock.shared.generated.resources.food_block_top_1x4_brown
import blockbyblock.shared.generated.resources.food_block_top_1x4_green
import blockbyblock.shared.generated.resources.food_block_top_1x4_orange
import blockbyblock.shared.generated.resources.food_block_top_1x4_red
import blockbyblock.shared.generated.resources.food_block_top_1x4_white
import blockbyblock.shared.generated.resources.food_block_top_1x4_yellow
import blockbyblock.shared.generated.resources.food_block_top_2x2_brown
import blockbyblock.shared.generated.resources.food_block_top_2x2_green
import blockbyblock.shared.generated.resources.food_block_top_2x2_orange
import blockbyblock.shared.generated.resources.food_block_top_2x2_red
import blockbyblock.shared.generated.resources.food_block_top_2x2_white
import blockbyblock.shared.generated.resources.food_block_top_2x2_yellow
import blockbyblock.shared.generated.resources.food_block_top_2x4_brown
import blockbyblock.shared.generated.resources.food_block_top_2x4_green
import blockbyblock.shared.generated.resources.food_block_top_2x4_orange
import blockbyblock.shared.generated.resources.food_block_top_2x4_red
import blockbyblock.shared.generated.resources.food_block_top_2x4_white
import blockbyblock.shared.generated.resources.food_block_top_2x4_yellow
import blockbyblock.shared.generated.resources.food_block_top_3x4_brown
import blockbyblock.shared.generated.resources.food_block_top_3x4_green
import blockbyblock.shared.generated.resources.food_block_top_3x4_orange
import blockbyblock.shared.generated.resources.food_block_top_3x4_red
import blockbyblock.shared.generated.resources.food_block_top_3x4_white
import blockbyblock.shared.generated.resources.food_block_top_3x4_yellow
import com.dahee.blockbyblock.domain.model.BlockSizeCategory
import com.dahee.blockbyblock.domain.model.FoodBlockColorType
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Returns the exact 2D top-down food block drawable based on mold capacity size category:
 * - MINI: 2x2 (0 ~ 100ml)
 * - SMALL: 1x4 (101 ~ 200ml)
 * - MEDIUM: 2x4 (201 ~ 399ml)
 * - LARGE: 3x4 (400ml ~)
 */
fun getFoodBlockTopDrawable(colorHex: String, moldCapacityMl: Int = 250): DrawableResource {
    val colorType = FoodBlockColorType.fromHex(colorHex)
    val category = BlockSizeCategory.fromCapacity(moldCapacityMl)

    return when (category) {
        BlockSizeCategory.LARGE -> when (colorType) {
            FoodBlockColorType.RED -> Res.drawable.food_block_top_3x4_red
            FoodBlockColorType.YELLOW -> Res.drawable.food_block_top_3x4_yellow
            FoodBlockColorType.GREEN -> Res.drawable.food_block_top_3x4_green
            FoodBlockColorType.BROWN -> Res.drawable.food_block_top_3x4_brown
            FoodBlockColorType.WHITE -> Res.drawable.food_block_top_3x4_white
            FoodBlockColorType.ORANGE -> Res.drawable.food_block_top_3x4_orange
        }
        BlockSizeCategory.MEDIUM -> when (colorType) {
            FoodBlockColorType.RED -> Res.drawable.food_block_top_2x4_red
            FoodBlockColorType.YELLOW -> Res.drawable.food_block_top_2x4_yellow
            FoodBlockColorType.GREEN -> Res.drawable.food_block_top_2x4_green
            FoodBlockColorType.BROWN -> Res.drawable.food_block_top_2x4_brown
            FoodBlockColorType.WHITE -> Res.drawable.food_block_top_2x4_white
            FoodBlockColorType.ORANGE -> Res.drawable.food_block_top_2x4_orange
        }
        BlockSizeCategory.SMALL -> when (colorType) {
            FoodBlockColorType.RED -> Res.drawable.food_block_top_1x4_red
            FoodBlockColorType.YELLOW -> Res.drawable.food_block_top_1x4_yellow
            FoodBlockColorType.GREEN -> Res.drawable.food_block_top_1x4_green
            FoodBlockColorType.BROWN -> Res.drawable.food_block_top_1x4_brown
            FoodBlockColorType.WHITE -> Res.drawable.food_block_top_1x4_white
            FoodBlockColorType.ORANGE -> Res.drawable.food_block_top_1x4_orange
        }
        BlockSizeCategory.MINI -> when (colorType) {
            FoodBlockColorType.RED -> Res.drawable.food_block_top_2x2_red
            FoodBlockColorType.YELLOW -> Res.drawable.food_block_top_2x2_yellow
            FoodBlockColorType.GREEN -> Res.drawable.food_block_top_2x2_green
            FoodBlockColorType.BROWN -> Res.drawable.food_block_top_2x2_brown
            FoodBlockColorType.WHITE -> Res.drawable.food_block_top_2x2_white
            FoodBlockColorType.ORANGE -> Res.drawable.food_block_top_2x2_orange
        }
    }
}

/**
 * Top-down Toy Food Block component loaded directly from drawable resources matching reference.
 */
@Composable
fun FoodBlockTopView(
    colorHex: String,
    modifier: Modifier = Modifier,
    moldCapacityMl: Int = 250,
    width: Dp? = null,
    height: Dp = 96.dp
) {
    val category = BlockSizeCategory.fromCapacity(moldCapacityMl)
    val drawableRes = getFoodBlockTopDrawable(colorHex = colorHex, moldCapacityMl = moldCapacityMl)

    val finalWidth = width ?: when (category) {
        BlockSizeCategory.LARGE -> height * (72f / 96f)  // 3x4 라지
        BlockSizeCategory.SMALL -> height * (26f / 96f)  // 1x4 스몰
        BlockSizeCategory.MINI -> height * (50f / 96f)   // 2x2 미니
        BlockSizeCategory.MEDIUM -> height * (50f / 96f) // 2x4 미디엄
    }

    val finalHeight = if (category == BlockSizeCategory.MINI && width == null) height * (50f / 96f) else height

    Box(
        modifier = modifier
            .width(finalWidth)
            .height(finalHeight),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(drawableRes),
            contentDescription = "Top-view Food Block",
            modifier = Modifier.fillMaxSize()
        )
    }
}
