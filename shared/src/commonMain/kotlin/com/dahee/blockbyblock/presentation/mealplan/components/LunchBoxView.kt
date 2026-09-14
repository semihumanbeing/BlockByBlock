package com.dahee.blockbyblock.presentation.mealplan.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dahee.blockbyblock.core.i18n.LocalStrings
import com.dahee.blockbyblock.core.theme.AppColors
import com.dahee.blockbyblock.domain.model.FoodBlock
import com.dahee.blockbyblock.domain.model.MealBlockItem
import com.dahee.blockbyblock.domain.model.MealBlockStatus
import com.dahee.blockbyblock.domain.model.determineBlockStatusesIndexed
import com.dahee.blockbyblock.presentation.block.components.FoodBlockTopView

/**
 * Reusable Bento Lunch Box (도시락통) UI Component.
 *
 * Provides the authentic lunchbox frame with:
 * - Ivory rim body with smooth rounded corners
 * - Left & right latch/buckle details
 * - Inner lunchbox tray floor
 * - Top-view food block rendering with horizontal scrolling
 * - Adaptive layout: supports dynamic auto-expansion (for Dialog) and fixed-proportion fill (for Slot Cards)
 */
@Composable
fun BentoLunchBoxView(
    blocks: List<MealBlockItem>,
    modifier: Modifier = Modifier,
    blockHeight: Dp = 76.dp,
    isDynamicExpandable: Boolean = false,
    allFoodBlocks: List<FoodBlock>? = null,
    onBlockClick: ((MealBlockItem) -> Unit)? = null,
    emptyPlaceholder: (@Composable () -> Unit)? = null
) {
    val bentoModifier = if (isDynamicExpandable) {
        Modifier
            .wrapContentWidth()
            .animateContentSize(animationSpec = spring(dampingRatio = 0.8f, stiffness = 380f))
    } else {
        Modifier.fillMaxSize()
    }

    val bodyModifier = if (isDynamicExpandable) {
        Modifier
            .wrapContentWidth()
            .widthIn(min = 236.dp)
            .padding(horizontal = 4.dp)
    } else {
        Modifier
            .fillMaxSize()
            .padding(horizontal = 3.dp)
    }

    val trayModifier = if (isDynamicExpandable) {
        Modifier
            .wrapContentWidth()
            .widthIn(min = 220.dp)
            .height(124.dp)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    } else {
        Modifier
            .fillMaxSize()
            .padding(horizontal = 6.dp, vertical = 2.dp)
    }

    val blockStatuses = remember(blocks, allFoodBlocks) {
        determineBlockStatusesIndexed(blocks, allFoodBlocks)
    }

    BoxWithConstraints(
        modifier = if (isDynamicExpandable) modifier.fillMaxWidth() else modifier,
        contentAlignment = Alignment.Center
    ) {
        val containerHeight = maxHeight
        val latchHeight = if (isDynamicExpandable) 28.dp else (containerHeight * 0.48f).coerceIn(16.dp, 28.dp)
        val outerCornerRadius = if (isDynamicExpandable) 20.dp else (containerHeight * 0.22f).coerceIn(10.dp, 20.dp)
        val innerCornerRadius = if (isDynamicExpandable) 14.dp else (containerHeight * 0.16f).coerceIn(7.dp, 14.dp)

        Box(
            modifier = bentoModifier,
            contentAlignment = Alignment.Center
        ) {
            // Main Bento Body (도시락 외관 본체 - 웜 아이보리 베이지)
            Box(
                modifier = bodyModifier
                    .clip(RoundedCornerShape(outerCornerRadius))
                    .background(Color(0xFFEBE6DF))
                    .border(1.5.dp, Color(0xFFDBD3C8), RoundedCornerShape(outerCornerRadius))
                    .padding(if (isDynamicExpandable) 5.dp else 3.dp),
                contentAlignment = Alignment.Center
            ) {
                // Bento Inner Tray Floor (도시락 내부 바닥 - 소프트 화이트)
                Box(
                    modifier = trayModifier
                        .clip(RoundedCornerShape(innerCornerRadius))
                        .background(Color(0xFFFAF8F5))
                        .border(1.dp, Color(0xFFE2DDD5), RoundedCornerShape(innerCornerRadius))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    contentAlignment = if (blocks.isNotEmpty()) Alignment.CenterStart else Alignment.Center
                ) {
                    if (blocks.isNotEmpty()) {
                        if (isDynamicExpandable) {
                            Row(
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                blocks.forEachIndexed { index, item ->
                                    key(item.instanceId) {
                                        val status = blockStatuses.getOrElse(index) { MealBlockStatus.AVAILABLE }
                                        val blockModifier = if (onBlockClick != null) {
                                            Modifier
                                                .pointerHoverIcon(PointerIcon.Hand)
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null,
                                                    onClick = { onBlockClick(item) }
                                                )
                                        } else {
                                            Modifier.clip(RoundedCornerShape(8.dp))
                                        }

                                        BentoFoodBlockItem(
                                            item = item,
                                            status = status,
                                            height = blockHeight,
                                            modifier = blockModifier
                                        )
                                    }
                                }
                            }
                        } else {
                            BoxWithConstraints(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                val computedHeight = (maxHeight - 2.dp).coerceAtLeast(20.dp)

                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.Start),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    blocks.forEachIndexed { index, item ->
                                        key(item.instanceId) {
                                            val status = blockStatuses.getOrElse(index) { MealBlockStatus.AVAILABLE }
                                            val blockModifier = if (onBlockClick != null) {
                                                Modifier
                                                    .pointerHoverIcon(PointerIcon.Hand)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable(
                                                        interactionSource = remember { MutableInteractionSource() },
                                                        indication = null,
                                                        onClick = { onBlockClick(item) }
                                                    )
                                            } else {
                                                Modifier.clip(RoundedCornerShape(6.dp))
                                            }

                                            BentoFoodBlockItem(
                                                item = item,
                                                status = status,
                                                height = computedHeight,
                                                modifier = blockModifier
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else if (emptyPlaceholder != null) {
                        emptyPlaceholder()
                    } else {
                        Text(
                            text = "+",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextMuted.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // Left Side Latch / Buckle (왼쪽 잠금 버클 - 베이지 그레이)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = if (isDynamicExpandable) (-1).dp else 0.dp)
                    .size(width = 4.dp, height = latchHeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFDBD3C8))
                    .border(0.75.dp, Color(0xFFC7BEB2), RoundedCornerShape(2.dp))
            )

            // Right Side Latch / Buckle (오른쪽 잠금 버클 - 베이지 그레이)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = if (isDynamicExpandable) 1.dp else 0.dp)
                    .size(width = 4.dp, height = latchHeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFDBD3C8))
                    .border(0.75.dp, Color(0xFFC7BEB2), RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
private fun BentoFoodBlockItem(
    item: MealBlockItem,
    status: MealBlockStatus,
    height: Dp,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // 1. Food block top-down image (30% alpha if depleted, 45% alpha if deleted, full alpha otherwise)
        Box(
            modifier = when (status) {
                MealBlockStatus.OUT_OF_STOCK -> Modifier.alpha(0.3f)
                MealBlockStatus.DELETED -> Modifier.alpha(0.45f)
                MealBlockStatus.AVAILABLE -> Modifier
            }
        ) {
            FoodBlockTopView(
                colorHex = item.blockColorHex,
                moldCapacityMl = item.moldCapacityMl,
                height = height,
                isGrayscale = (status == MealBlockStatus.DELETED)
            )
        }

        // 2. Depleted status: Dashed border & Orange [소진됨 0개] badge
        if (status == MealBlockStatus.OUT_OF_STOCK) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .drawBehind {
                        val strokeWidth = if (height < 50.dp) 1.dp.toPx() else 1.5.dp.toPx()
                        val dashLength = if (height < 50.dp) 4.dp.toPx() else 6.dp.toPx()
                        val gapLength = if (height < 50.dp) 3.dp.toPx() else 4.dp.toPx()
                        val cornerRadius = if (height < 50.dp) 4.dp.toPx() else 6.dp.toPx()
                        drawRoundRect(
                            color = Color(0xFFF97316),
                            topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                            size = Size(size.width - strokeWidth, size.height - strokeWidth),
                            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                            style = Stroke(
                                width = strokeWidth,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength, gapLength), 0f)
                            )
                        )
                    }
            )

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFF97316))
                    .padding(
                        horizontal = if (height < 50.dp) 2.dp else 4.dp,
                        vertical = if (height < 50.dp) 1.dp else 2.dp
                    )
            ) {
                Text(
                    text = if (height < 45.dp) strings.depletedShortBadge else strings.depletedBlockBadge,
                    fontSize = if (height < 50.dp) 7.sp else 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1
                )
            }
        }

        // 3. Deleted or depleted status: Gray badge
        if (status == MealBlockStatus.DELETED) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF6B7280).copy(alpha = 0.9f))
                    .padding(
                        horizontal = if (height < 50.dp) 2.dp else 4.dp,
                        vertical = if (height < 50.dp) 1.dp else 2.dp
                    )
            ) {
                Text(
                    text = strings.deletedBlockBadge,
                    fontSize = if (height < 50.dp) 7.sp else 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
            }
        }
    }
}
