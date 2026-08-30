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
import blockbyblock.shared.generated.resources.food_block_3d_1x4_green
import blockbyblock.shared.generated.resources.food_block_3d_1x4_orange
import blockbyblock.shared.generated.resources.food_block_3d_1x4_red
import blockbyblock.shared.generated.resources.food_block_3d_1x4_white
import blockbyblock.shared.generated.resources.food_block_3d_1x4_yellow
import blockbyblock.shared.generated.resources.food_block_3d_2x2_green
import blockbyblock.shared.generated.resources.food_block_3d_2x2_orange
import blockbyblock.shared.generated.resources.food_block_3d_2x2_red
import blockbyblock.shared.generated.resources.food_block_3d_2x2_white
import blockbyblock.shared.generated.resources.food_block_3d_2x2_yellow
import blockbyblock.shared.generated.resources.food_block_3d_2x4_green
import blockbyblock.shared.generated.resources.food_block_3d_2x4_orange
import blockbyblock.shared.generated.resources.food_block_3d_2x4_red
import blockbyblock.shared.generated.resources.food_block_3d_2x4_white
import blockbyblock.shared.generated.resources.food_block_3d_2x4_yellow
import blockbyblock.shared.generated.resources.food_block_3d_3x4_green
import blockbyblock.shared.generated.resources.food_block_3d_3x4_orange
import blockbyblock.shared.generated.resources.food_block_3d_3x4_red
import blockbyblock.shared.generated.resources.food_block_3d_3x4_white
import blockbyblock.shared.generated.resources.food_block_3d_3x4_yellow
import com.dahee.blockbyblock.domain.model.BlockSizeCategory
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Returns the exact 3D food block drawable based on mold capacity size category:
 * - MINI (미니): 2x2 (0 ~ 100ml)
 * - SMALL (스몰): 1x4 (101 ~ 200ml)
 * - MEDIUM (미디엄): 2x4 (201 ~ 399ml)
 * - LARGE (라지): 3x4 (400ml ~)
 */
fun getFoodBlock3DDrawable(colorHex: String, moldCapacityMl: Int = 250): DrawableResource {
    val cleanHex = colorHex.uppercase().trim()
    val isRed = cleanHex.contains("EA4832") || cleanHex.contains("E53935") || cleanHex.contains("D32F2F") || cleanHex.contains("FF0000")
    val isYellow = cleanHex.contains("FDD835") || cleanHex.contains("FBC02D") || cleanHex.contains("FFEB3B")
    val isGreen = cleanHex.contains("44923E") || cleanHex.contains("43A047") || cleanHex.contains("66BB6A") || cleanHex.contains("4CAF50") || cleanHex.contains("2E7D32")
    val isWhite = cleanHex.contains("F4F5F3") || cleanHex.contains("F5F5F0") || cleanHex.contains("FFFFFF") || cleanHex.contains("FFF8E1") || cleanHex.contains("F5F0E6") || cleanHex.contains("F0E5D8")

    val category = BlockSizeCategory.fromCapacity(moldCapacityMl)

    return when (category) {
        // LARGE (라지: 3x4, 400ml ~)
        BlockSizeCategory.LARGE -> when {
            isRed -> Res.drawable.food_block_3d_3x4_red
            isYellow -> Res.drawable.food_block_3d_3x4_yellow
            isGreen -> Res.drawable.food_block_3d_3x4_green
            isWhite -> Res.drawable.food_block_3d_3x4_white
            else -> Res.drawable.food_block_3d_3x4_orange
        }
        // MEDIUM (미디엄: 2x4, 201 ~ 399ml)
        BlockSizeCategory.MEDIUM -> when {
            isRed -> Res.drawable.food_block_3d_2x4_red
            isYellow -> Res.drawable.food_block_3d_2x4_yellow
            isGreen -> Res.drawable.food_block_3d_2x4_green
            isWhite -> Res.drawable.food_block_3d_2x4_white
            else -> Res.drawable.food_block_3d_2x4_orange
        }
        // SMALL (스몰: 1x4, 101 ~ 200ml)
        BlockSizeCategory.SMALL -> when {
            isRed -> Res.drawable.food_block_3d_1x4_red
            isYellow -> Res.drawable.food_block_3d_1x4_yellow
            isGreen -> Res.drawable.food_block_3d_1x4_green
            isWhite -> Res.drawable.food_block_3d_1x4_white
            else -> Res.drawable.food_block_3d_1x4_orange
        }
        // MINI (미니: 2x2, 0 ~ 100ml)
        BlockSizeCategory.MINI -> when {
            isRed -> Res.drawable.food_block_3d_2x2_red
            isYellow -> Res.drawable.food_block_3d_2x2_yellow
            isGreen -> Res.drawable.food_block_3d_2x2_green
            isWhite -> Res.drawable.food_block_3d_2x2_white
            else -> Res.drawable.food_block_3d_2x2_orange
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
