package com.dahee.blockbyblock.presentation.equipment.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dahee.blockbyblock.domain.model.MoldGridPreset

/**
 * 3D Toy block style silicone freezer mold view (supports real-time dynamic color and compartment layout)
 *
 * @param preset Mold preset specification (ML_500, ML_250, ML_125, ML_75, CUSTOM)
 * @param moldColor User selected mold color (rendered dynamically)
 * @param cellCount User configured slot count (optional, partitions grid when provided)
 */
@Composable
fun MoldView(
    preset: MoldGridPreset,
    moldColor: Color,
    modifier: Modifier = Modifier,
    cellCount: Int? = null,
    size: Dp = 80.dp
) {
    val (rows, cols) = if (cellCount != null) {
        when (cellCount) {
            1 -> Pair(1, 1)
            2 -> Pair(2, 1)
            3 -> Pair(3, 1)
            4 -> Pair(4, 1) // 4 slots: 4 vertical lines
            6 -> Pair(6, 1) // 6 slots: 6 vertical lines
            8 -> Pair(4, 2) // 8 slots: 4 rows x 2 cols
            12 -> Pair(4, 3) // 12 slots: 4 rows x 3 cols ice tray
            15 -> Pair(5, 3) // 15 slots: 5 rows x 3 cols ice tray
            16 -> Pair(4, 4) // 16 slots: 4 rows x 4 cols ice tray
            24 -> Pair(6, 4) // 24 slots: 6 rows x 4 cols ice tray
            else -> {
                if (cellCount <= 6) Pair(cellCount, 1)
                else if (cellCount <= 10) Pair((cellCount + 1) / 2, 2)
                else Pair((cellCount + 2) / 3, 3)
            }
        }
    } else {
        when (preset) {
            MoldGridPreset.ML_500 -> Pair(1, 1)
            MoldGridPreset.ML_250 -> Pair(4, 1) // 4 vertical lines
            MoldGridPreset.ML_125 -> Pair(6, 1) // 6 vertical lines
            MoldGridPreset.ML_30 -> Pair(5, 3)  // 15 slot ice tray
            MoldGridPreset.CUSTOM -> Pair(4, 1) // 4 vertical lines
        }
    }

    Box(
        modifier = modifier.size(size).aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            val padX = w * 0.08f
            val padY = h * 0.08f
            val trayW = w - (padX * 2)
            val trayH = h - (padY * 2)
            val cornerR = w * 0.10f

            // 1. Soft 3D Drop Shadow
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.14f),
                topLeft = Offset(padX, padY + (h * 0.035f)),
                size = Size(trayW, trayH),
                cornerRadius = CornerRadius(cornerR, cornerR)
            )

            // 2. Block Mold Base Rim Layer (Darker Base Plastic Rim)
            drawRoundRect(
                color = moldColor,
                topLeft = Offset(padX, padY + (h * 0.018f)),
                size = Size(trayW, trayH),
                cornerRadius = CornerRadius(cornerR, cornerR)
            )
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.18f),
                topLeft = Offset(padX, padY + (h * 0.018f)),
                size = Size(trayW, trayH),
                cornerRadius = CornerRadius(cornerR, cornerR)
            )

            // 3. Main Silicone Tray Body (Vibrant Mold Body)
            drawRoundRect(
                color = moldColor,
                topLeft = Offset(padX, padY),
                size = Size(trayW, trayH),
                cornerRadius = CornerRadius(cornerR, cornerR)
            )

            // 3-1. Top Gloss Sheen
            drawRoundRect(
                color = Color.White.copy(alpha = 0.25f),
                topLeft = Offset(padX + 2f, padY + 2f),
                size = Size(trayW - 4f, trayH * 0.32f),
                cornerRadius = CornerRadius(cornerR * 0.8f, cornerR * 0.8f)
            )

            // 4. Block Studs - Circular studs on top & bottom rims
            val studRadius = w * 0.030f
            val numStuds = 5
            val studSpacingX = (trayW - (studRadius * 4)) / (numStuds - 1)

            for (i in 0 until numStuds) {
                val sx = padX + (studRadius * 2) + (i * studSpacingX)
                // Top stud
                drawBlockStud(
                    center = Offset(sx, padY + (studRadius * 1.15f)),
                    radius = studRadius,
                    color = moldColor
                )
                // Bottom stud
                drawBlockStud(
                    center = Offset(sx, padY + trayH - (studRadius * 1.15f)),
                    radius = studRadius,
                    color = moldColor
                )
            }

            // Left & Right side studs
            drawBlockStud(
                center = Offset(padX + (studRadius * 1.15f), padY + trayH / 2),
                radius = studRadius,
                color = moldColor
            )
            drawBlockStud(
                center = Offset(padX + trayW - (studRadius * 1.15f), padY + trayH / 2),
                radius = studRadius,
                color = moldColor
            )

            // 5. Embedded Steel Wire Rim
            val steelPad = w * 0.050f
            drawRoundRect(
                color = Color.White.copy(alpha = 0.70f),
                topLeft = Offset(padX + steelPad, padY + steelPad),
                size = Size(trayW - (steelPad * 2), trayH - (steelPad * 2)),
                cornerRadius = CornerRadius(cornerR * 0.65f, cornerR * 0.65f),
                style = Stroke(width = w * 0.015f)
            )
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.10f),
                topLeft = Offset(padX + steelPad + 1f, padY + steelPad + 1.2f),
                size = Size(trayW - (steelPad * 2), trayH - (steelPad * 2)),
                cornerRadius = CornerRadius(cornerR * 0.65f, cornerR * 0.65f),
                style = Stroke(width = w * 0.010f)
            )

            // 6. Internal Compartments Area
            val innerPad = w * 0.078f
            val ix0 = padX + innerPad
            val iy0 = padY + innerPad
            val iw = trayW - (innerPad * 2)
            val ih = trayH - (innerPad * 2)

            val gap = if (rows <= 4 && cols == 1) w * 0.022f else if (rows <= 6 && cols == 1) w * 0.016f else w * 0.014f
            val cellW = (iw - (gap * (cols - 1))) / cols
            val cellH = (ih - (gap * (rows - 1))) / rows
            val cellR = if (rows <= 4 && cols == 1) w * 0.030f else w * 0.018f

            if (preset == MoldGridPreset.CUSTOM && cellCount == null) {
                // Custom mold slot with plus icon
                for (r in 0 until rows) {
                    for (c in 0 until cols) {
                        val cx = ix0 + c * (cellW + gap)
                        val cy = iy0 + r * (cellH + gap)
                        drawCompartmentCavity(cx, cy, cellW, cellH, cellR, moldColor)

                        // Plus icon
                        val midX = cx + cellW / 2
                        val midY = cy + cellH / 2
                        val arm = cellH * 0.25f
                        drawLine(
                            color = Color.White.copy(alpha = 0.9f),
                            start = Offset(midX - arm, midY),
                            end = Offset(midX + arm, midY),
                            strokeWidth = 3.0f
                        )
                        drawLine(
                            color = Color.White.copy(alpha = 0.9f),
                            start = Offset(midX, midY - arm),
                            end = Offset(midX, midY + arm),
                            strokeWidth = 3.0f
                        )
                    }
                }
            } else {
                // Compartment cavity rendering
                for (r in 0 until rows) {
                    for (c in 0 until cols) {
                        val cx = ix0 + c * (cellW + gap)
                        val cy = iy0 + r * (cellH + gap)
                        drawCompartmentCavity(cx, cy, cellW, cellH, cellR, moldColor)
                    }
                }
            }
        }
    }
}

/**
 * Draw 3D Block Stud (Circular embossed stud)
 */
private fun DrawScope.drawBlockStud(
    center: Offset,
    radius: Float,
    color: Color
) {
    // 1. Stud bottom shadow (Drop Shadow)
    drawCircle(
        color = Color.Black.copy(alpha = 0.24f),
        radius = radius,
        center = Offset(center.x, center.y + (radius * 0.35f))
    )

    // 2. Stud base cylinder (Stud Base)
    drawCircle(
        color = color,
        radius = radius,
        center = center
    )

    // 3. Stud top bevel highlight rim
    drawCircle(
        color = Color.White.copy(alpha = 0.45f),
        radius = radius * 0.85f,
        center = Offset(center.x - (radius * 0.12f), center.y - (radius * 0.12f)),
        style = Stroke(width = radius * 0.22f)
    )

    // 4. Center stud highlight detail
    drawCircle(
        color = Color.White.copy(alpha = 0.3f),
        radius = radius * 0.35f,
        center = Offset(center.x - (radius * 0.1f), center.y - (radius * 0.1f))
    )
}

/**
 * Draw 3D Compartment Cavity (Deep molded slot)
 */
private fun DrawScope.drawCompartmentCavity(
    cx: Float,
    cy: Float,
    cellW: Float,
    cellH: Float,
    cellR: Float,
    color: Color
) {
    // 1. Deep slot inner shadow (Top/Left Shadow)
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.32f),
        topLeft = Offset(cx, cy),
        size = Size(cellW, cellH),
        cornerRadius = CornerRadius(cellR, cellR)
    )

    // 2. Deep cavity floor
    val inset = 2.5f
    drawRoundRect(
        color = color.copy(alpha = 0.82f),
        topLeft = Offset(cx + inset, cy + inset),
        size = Size(cellW - (inset * 2), cellH - (inset * 2)),
        cornerRadius = CornerRadius(cellR * 0.85f, cellR * 0.85f)
    )

    // 3. Bottom-right specular light bounce
    drawRoundRect(
        color = Color.White.copy(alpha = 0.22f),
        topLeft = Offset(cx + cellW * 0.2f, cy + cellH * 0.55f),
        size = Size(cellW * 0.72f, cellH * 0.38f),
        cornerRadius = CornerRadius(cellR * 0.7f, cellR * 0.7f)
    )
}
