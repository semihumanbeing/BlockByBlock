package com.dahee.blockbyblock.presentation.equipment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import com.dahee.blockbyblock.core.theme.AppColors
import com.dahee.blockbyblock.presentation.equipment.screens.EquipmentListScreen
import com.dahee.blockbyblock.presentation.equipment.screens.EquipmentOnboardingScreen
import com.dahee.blockbyblock.presentation.equipment.screens.EquipmentSetupScreen
import com.dahee.blockbyblock.presentation.equipment.state.EquipmentScreenMode

@Composable
fun EquipmentScreen(
    viewModel: EquipmentViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    // Clear focus / dismiss keyboard on background tap
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                focusManager.clearFocus()
            }
    ) {
        when (uiState.screenMode) {
            EquipmentScreenMode.ONBOARDING -> {
                // Screen 1: "Let's register equipment" onboarding
                EquipmentOnboardingScreen(
                    onStartSetup = { viewModel.onStartSetupFromOnboarding() }
                )
            }
            EquipmentScreenMode.SETUP -> {
                // Screen 2: Mold configuration + cooking tool selector + [Save]
                EquipmentSetupScreen(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }
            EquipmentScreenMode.LIST -> {
                // Screen 3: My molds & cooking tools list + top-right [Edit All] button
                EquipmentListScreen(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }
        }
    }
}
