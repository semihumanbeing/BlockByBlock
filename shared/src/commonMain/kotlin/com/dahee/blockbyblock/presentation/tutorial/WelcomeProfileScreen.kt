package com.dahee.blockbyblock.presentation.tutorial

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import blockbyblock.shared.generated.resources.Res
import blockbyblock.shared.generated.resources.food_block_3d_1x4_green
import blockbyblock.shared.generated.resources.food_block_3d_2x2_yellow
import blockbyblock.shared.generated.resources.food_block_3d_2x4_orange
import blockbyblock.shared.generated.resources.food_block_3d_3x4_red
import com.dahee.blockbyblock.core.i18n.LocalStrings
import com.dahee.blockbyblock.core.theme.AppColors
import com.dahee.blockbyblock.core.ui.AppButton
import com.dahee.blockbyblock.core.ui.AppCard
import com.dahee.blockbyblock.core.ui.AppTextField
import com.dahee.blockbyblock.core.ui.ButtonVariant
import org.jetbrains.compose.resources.painterResource

@Composable
fun WelcomeProfileScreen(
    onStart: (String) -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val focusManager = LocalFocusManager.current
    var nicknameInput by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .imePadding()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
            }
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Top Right Skip Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopEnd),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = strings.tutorialSkipBtn,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextMuted,
                modifier = Modifier
                    .pointerHoverIcon(PointerIcon.Hand)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onSkip
                    )
                    .padding(8.dp)
            )
        }

        // Center Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Stacked 3D Lego Food Blocks Illustration
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                // Bottom Layer: 3x4 Red Block (Large)
                Image(
                    painter = painterResource(Res.drawable.food_block_3d_3x4_red),
                    contentDescription = "Red Lego Block",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(150.dp)
                        .offset(x = (-10).dp, y = 35.dp)
                )

                // Middle Layer: 2x4 Orange Block (Medium)
                Image(
                    painter = painterResource(Res.drawable.food_block_3d_2x4_orange),
                    contentDescription = "Orange Lego Block",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(130.dp)
                        .offset(x = 18.dp, y = (-5).dp)
                )

                // Upper Layer Left: 1x4 Green Block (Small)
                Image(
                    painter = painterResource(Res.drawable.food_block_3d_1x4_green),
                    contentDescription = "Green Lego Block",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(105.dp)
                        .offset(x = (-38).dp, y = (-40).dp)
                )

                // Upper Layer Right: 2x2 Yellow Block (Mini)
                Image(
                    painter = painterResource(Res.drawable.food_block_3d_2x2_yellow),
                    contentDescription = "Yellow Lego Block",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(85.dp)
                        .offset(x = 35.dp, y = (-55).dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Main Title & Subtitle
            Text(
                text = "WELCOME!",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AppColors.TextPrimary,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = strings.tutorialWelcomeSubtitle,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Empty Nickname Input Box (No initial placeholder text that breaks on web)
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                padding = 16.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    AppTextField(
                        value = nicknameInput,
                        onValueChange = { nicknameInput = it },
                        placeholder = "",
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                if (nicknameInput.isNotBlank()) {
                                    onStart(nicknameInput.trim())
                                }
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    AppButton(
                        text = strings.tutorialStartBtn,
                        variant = ButtonVariant.PRIMARY,
                        enabled = nicknameInput.isNotBlank(),
                        onClick = {
                            if (nicknameInput.isNotBlank()) {
                                onStart(nicknameInput.trim())
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
