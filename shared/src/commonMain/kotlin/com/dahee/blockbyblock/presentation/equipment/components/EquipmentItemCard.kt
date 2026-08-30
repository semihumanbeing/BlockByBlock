package com.dahee.blockbyblock.presentation.equipment.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dahee.blockbyblock.core.theme.AppColors
import com.dahee.blockbyblock.core.ui.AppCard
import com.dahee.blockbyblock.core.ui.MoldGridVisualizer
import com.dahee.blockbyblock.domain.model.Equipment
import com.dahee.blockbyblock.domain.model.EquipmentCategory

@Composable
fun EquipmentItemCard(
    equipment: Equipment,
    onClick: () -> Unit,
    onIncreaseQuantity: () -> Unit,
    onDecreaseQuantity: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        padding = 16.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Visual Area (Mold: Grid, Cooking Tool: Icon)
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColors.SurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (equipment.category == EquipmentCategory.MOLD) {
                    val moldColor = AppColors.hexToColor(equipment.moldColorHex)
                    MoldGridVisualizer(
                        rows = equipment.gridRows,
                        cols = equipment.gridCols,
                        moldColor = moldColor,
                        cellSize = 11.dp,
                        cellSpacing = 2.dp
                    )
                } else {
                    equipment.toolType?.let {
                        CookingToolVisual(type = it, size = 44.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Center Info Area
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = equipment.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (equipment.category == EquipmentCategory.MOLD) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${equipment.displayCapacity}ml",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )

                        Text(
                            text = "·",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextMuted
                        )

                        Text(
                            text = "${equipment.cellCount}칸",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.TextSecondary
                        )
                    }
                } else {
                    Text(
                        text = if (equipment.memo.isNotBlank()) equipment.memo else (equipment.toolType?.displayName ?: "Cooking tool"),
                        fontSize = 13.sp,
                        color = AppColors.TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Right Stepper Area ([-] [N] [+])
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(AppColors.SurfaceVariant)
                    .border(1.dp, AppColors.Border, RoundedCornerShape(8.dp))
                    .padding(horizontal = 3.dp, vertical = 2.dp)
            ) {
                // Decrement
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onDecreaseQuantity() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "-",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (equipment.quantity > 0) AppColors.TextPrimary else AppColors.TextMuted
                    )
                }

                // Current quantity
                Text(
                    text = "${equipment.quantity}개",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )

                // Increment
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onIncreaseQuantity() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Primary
                    )
                }
            }
        }
    }
}
