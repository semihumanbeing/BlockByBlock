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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val focusManager = LocalFocusManager.current
    var nicknameInput by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .safeDrawingPadding()
            .imePadding()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
            },
        contentAlignment = Alignment.Center
    ) {
        // Center Content Container (Matching AuthScreen's position & visual hierarchy)
        Column(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Stacked 3D Food Blocks Illustration (Identical height, sizes, and offsets to AuthScreen)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.Center
            ) {
                // Bottom Layer: 3x4 Red Block (Large)
                Image(
                    painter = painterResource(Res.drawable.food_block_3d_3x4_red),
                    contentDescription = "Red Food Block",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(125.dp)
                        .offset(x = (-8).dp, y = 28.dp)
                )

                // Middle Layer: 2x4 Orange Block (Medium)
                Image(
                    painter = painterResource(Res.drawable.food_block_3d_2x4_orange),
                    contentDescription = "Orange Food Block",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(110.dp)
                        .offset(x = 15.dp, y = (-4).dp)
                )

                // Upper Layer Left: 1x4 Green Block (Small)
                Image(
                    painter = painterResource(Res.drawable.food_block_3d_1x4_green),
                    contentDescription = "Green Food Block",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(88.dp)
                        .offset(x = (-30).dp, y = (-32).dp)
                )

                // Upper Layer Right: 2x2 Yellow Block (Mini)
                Image(
                    painter = painterResource(Res.drawable.food_block_3d_2x2_yellow),
                    contentDescription = "Yellow Food Block",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(72.dp)
                        .offset(x = 28.dp, y = (-44).dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Title & Subtitle
            Text(
                text = "WELCOME!",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AppColors.TextPrimary,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = strings.tutorialWelcomeSubtitle,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Nickname Input Box
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                padding = 18.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    AppTextField(
                        value = nicknameInput,
                        onValueChange = { nicknameInput = it },
                        placeholder = strings.tutorialNicknamePlaceholder,
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
