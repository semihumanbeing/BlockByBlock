package com.dahee.blockbyblock.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dahee.blockbyblock.core.theme.AppColors

/**
 * Visual grid component to render mold dimensions (3x3, 2x3, 1x3, 1x1, etc.)
 */
@Composable
fun MoldGridVisualizer(
    rows: Int,
    cols: Int,
    moldColor: Color = AppColors.MoldSkyBlue,
    cellSize: Dp = 16.dp,
    cellSpacing: Dp = 3.dp,
    showCapacityLabel: Boolean = false,
    capacityText: String = "",
    modifier: Modifier = Modifier
) {
    val effectiveRows = rows.coerceAtLeast(1)
    val effectiveCols = cols.coerceAtLeast(1)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mold outer container
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(moldColor.copy(alpha = 0.35f))
                .border(
                    width = 1.5.dp,
                    color = moldColor,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(cellSpacing * 1.5f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(cellSpacing)
            ) {
                repeat(effectiveRows) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(cellSpacing)
                    ) {
                        repeat(effectiveCols) {
                            // Individual mold cell (pocket)
                            Box(
                                modifier = Modifier
                                    .size(cellSize)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(moldColor)
                                    .border(
                                        width = 1.dp,
                                        color = Color.Black.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                    }
                }
            }
        }

        if (showCapacityLabel && capacityText.isNotEmpty()) {
            Text(
                text = capacityText,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
