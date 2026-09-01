package com.dahee.blockbyblock.presentation.tutorial

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dahee.blockbyblock.core.i18n.LocalStrings
import com.dahee.blockbyblock.core.theme.AppColors
import com.dahee.blockbyblock.core.ui.AppButton
import com.dahee.blockbyblock.core.ui.ButtonVariant

@Composable
fun TutorialGuideBanner(
    currentStep: TutorialStep,
    hasAddedIngredient: Boolean = false,
    hasCreatedBlock: Boolean = false,
    onNextStep: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current

    val isVisible = currentStep != TutorialStep.WELCOME_PROFILE && currentStep != TutorialStep.COMPLETED

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut(),
        modifier = modifier
    ) {
        val isCongratulations = currentStep == TutorialStep.CONGRATULATIONS
        val (message, buttonText) = when (currentStep) {
            TutorialStep.EQUIPMENT_SETUP -> Pair(
                strings.tutorialStepEquipmentMsg,
                strings.tutorialStepEquipmentNextBtn
            )
            TutorialStep.INVENTORY_SETUP -> Pair(
                if (hasAddedIngredient) strings.tutorialStepInventoryAddedMsg else strings.tutorialStepInventoryMsg,
                strings.tutorialStepInventoryNextBtn
            )
            TutorialStep.CREATE_BLOCK -> Pair(
                if (hasCreatedBlock) strings.tutorialStepBlockCreatedMsg else strings.tutorialStepBlockMsg,
                strings.tutorialStepBlockNextBtn
            )
            TutorialStep.MEAL_PLAN -> Pair(
                strings.tutorialStepMealPlanMsg,
                strings.tutorialStepCompleteBtn
            )
            TutorialStep.CONGRATULATIONS -> Pair(
                strings.tutorialCongratulationsMsg,
                ""
            )
            else -> Pair("", "")
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .shadow(6.dp, RoundedCornerShape(16.dp), spotColor = AppColors.Shadow)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isCongratulations) AppColors.Primary else AppColors.PrimaryDark)
                .border(
                    1.dp,
                    if (isCongratulations) AppColors.BlockYellow else AppColors.Primary.copy(alpha = 0.8f),
                    RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isCongratulations) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Done",
                                tint = AppColors.BlockYellow,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = if (isCongratulations) "CONGRATULATIONS!" else "TUTORIAL GUIDE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isCongratulations) AppColors.BlockYellow else AppColors.PrimaryLight,
                            letterSpacing = 1.sp
                        )
                    }

                    if (!isCongratulations && currentStep != TutorialStep.EQUIPMENT_SETUP) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .pointerHoverIcon(PointerIcon.Hand)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = onSkip
                                )
                        ) {
                            Text(
                                text = strings.tutorialSkipBtn,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = strings.tutorialSkipBtn,
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Text(
                    text = message,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    lineHeight = 18.sp
                )

                if (!isCongratulations && buttonText.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        AppButton(
                            text = buttonText,
                            variant = ButtonVariant.WARM_YELLOW,
                            height = 36.dp,
                            onClick = onNextStep
                        )
                    }
                }
            }
        }
    }
}
