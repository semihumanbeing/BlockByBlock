package com.dahee.blockbyblock.presentation.equipment.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dahee.blockbyblock.core.theme.AppColors
import com.dahee.blockbyblock.domain.model.MoldCapacityUnit

@Composable
fun MoldUnitToggle(
    currentUnit: MoldCapacityUnit,
    onUnitChange: (MoldCapacityUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    val nextUnit = if (currentUnit == MoldCapacityUnit.ML) MoldCapacityUnit.CUP else MoldCapacityUnit.ML

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.Surface)
            .border(1.dp, AppColors.Border, RoundedCornerShape(20.dp))
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onUnitChange(nextUnit) }
            )
            .padding(2.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MoldCapacityUnit.entries.forEach { unit ->
            val isSelected = currentUnit == unit
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) AppColors.Primary else Color.Transparent)
                    .padding(horizontal = 9.dp, vertical = 3.5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = unit.symbol,
                    fontSize = 11.5.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else AppColors.TextSecondary
                )
            }
        }
    }
}
