package com.dahee.blockbyblock.presentation.mealplan.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dahee.blockbyblock.core.theme.AppColors
import com.dahee.blockbyblock.domain.model.MealBlockItem
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

    Box(
        modifier = if (isDynamicExpandable) modifier.fillMaxWidth() else modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = bentoModifier,
            contentAlignment = Alignment.Center
        ) {
            // Main Bento Body (도시락 외관 본체 - 웜 아이보리 베이지)
            Box(
                modifier = bodyModifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFEBE6DF))
                    .border(1.5.dp, Color(0xFFDBD3C8), RoundedCornerShape(20.dp))
                    .padding(if (isDynamicExpandable) 5.dp else 3.5.dp),
                contentAlignment = Alignment.Center
            ) {
                // Bento Inner Tray Floor (도시락 내부 바닥 - 소프트 화이트)
                Box(
                    modifier = trayModifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFFAF8F5))
                        .border(1.dp, Color(0xFFE2DDD5), RoundedCornerShape(14.dp))
                        .padding(horizontal = 5.dp, vertical = 3.dp),
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
                                blocks.forEach { item ->
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

                                    Box(modifier = blockModifier) {
                                        FoodBlockTopView(
                                            colorHex = item.blockColorHex,
                                            moldCapacityMl = item.moldCapacityMl,
                                            height = blockHeight
                                        )
                                    }
                                }
                            }
                        } else {
                            BoxWithConstraints(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                val computedHeight = (maxHeight - 2.dp).coerceAtLeast(48.dp)

                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    blocks.forEach { item ->
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

                                        Box(modifier = blockModifier) {
                                            FoodBlockTopView(
                                                colorHex = item.blockColorHex,
                                                moldCapacityMl = item.moldCapacityMl,
                                                height = computedHeight
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
                    .size(width = 5.dp, height = 28.dp)
                    .clip(RoundedCornerShape(2.5.dp))
                    .background(Color(0xFFDBD3C8))
                    .border(0.75.dp, Color(0xFFC7BEB2), RoundedCornerShape(2.5.dp))
            )

            // Right Side Latch / Buckle (오른쪽 잠금 버클 - 베이지 그레이)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = if (isDynamicExpandable) 1.dp else 0.dp)
                    .size(width = 5.dp, height = 28.dp)
                    .clip(RoundedCornerShape(2.5.dp))
                    .background(Color(0xFFDBD3C8))
                    .border(0.75.dp, Color(0xFFC7BEB2), RoundedCornerShape(2.5.dp))
            )
        }
    }
}
