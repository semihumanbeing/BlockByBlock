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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dahee.blockbyblock.core.theme.AppColors
import com.dahee.blockbyblock.core.ui.MoldGridVisualizer
import com.dahee.blockbyblock.domain.model.MoldGridPreset

@Composable
fun MoldPresetQuickAddRow(
    onQuickAdd: (MoldGridPreset, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val presetsWithColor = listOf(
        Pair(MoldGridPreset.ML_500, AppColors.MoldSkyBlue),
        Pair(MoldGridPreset.ML_250, AppColors.MoldMint),
        Pair(MoldGridPreset.ML_125, AppColors.MoldCoralPink),
        Pair(MoldGridPreset.ML_75, AppColors.MoldButterYellow)
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "표준 몰드 규격 빠른 추가",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary
            )
            Text(
                text = "클릭하여 즉시 추가 (+1)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.Primary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            presetsWithColor.forEach { (preset, color) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.Surface)
                        .border(1.dp, AppColors.Border, RoundedCornerShape(12.dp))
                        .clickable {
                            onQuickAdd(preset, AppColors.colorToHex(color))
                        }
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        MoldGridVisualizer(
                            rows = preset.rows,
                            cols = preset.cols,
                            moldColor = color,
                            cellSize = 9.dp,
                            cellSpacing = 2.dp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = preset.label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        )

                        Text(
                            text = "${preset.rows}x${preset.cols}",
                            fontSize = 10.sp,
                            color = AppColors.TextMuted
                        )
                    }
                }
            }
        }
    }
}
