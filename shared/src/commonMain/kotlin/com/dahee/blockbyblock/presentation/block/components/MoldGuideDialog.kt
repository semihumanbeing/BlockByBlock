package com.dahee.blockbyblock.presentation.block.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import blockbyblock.shared.generated.resources.Res
import blockbyblock.shared.generated.resources.mold_guide_en
import blockbyblock.shared.generated.resources.mold_guide_ko
import com.dahee.blockbyblock.core.i18n.AppLanguage
import com.dahee.blockbyblock.core.i18n.LocalAppLanguage
import com.dahee.blockbyblock.core.i18n.LocalStrings
import com.dahee.blockbyblock.core.theme.AppColors
import com.dahee.blockbyblock.core.ui.AppButton
import com.dahee.blockbyblock.core.ui.ButtonVariant
import org.jetbrains.compose.resources.painterResource

@Composable
fun MoldGuideDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val currentLanguage = LocalAppLanguage.current

    val guideDrawableRes = when (currentLanguage) {
        AppLanguage.KO -> Res.drawable.mold_guide_ko
        AppLanguage.EN -> Res.drawable.mold_guide_en
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.createBlockMoldGuideTitle,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF0F0F0))
                            .clickable { onDismiss() }
                            .pointerHoverIcon(PointerIcon.Hand),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF666666),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Guide Image
                Image(
                    painter = painterResource(guideDrawableRes),
                    contentDescription = strings.createBlockMoldGuideTitle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.FillWidth
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Close Button
                AppButton(
                    text = "확인",
                    onClick = onDismiss,
                    variant = ButtonVariant.PRIMARY,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
