package com.dahee.blockbyblock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dahee.blockbyblock.core.i18n.AppLanguage
import com.dahee.blockbyblock.core.i18n.LocalAppLanguage
import com.dahee.blockbyblock.core.i18n.LocalStrings
import com.dahee.blockbyblock.core.i18n.getStrings
import com.dahee.blockbyblock.core.theme.AppColors
import com.dahee.blockbyblock.core.theme.BlockByBlockTheme
import com.dahee.blockbyblock.data.repository.InMemoryEquipmentRepository
import com.dahee.blockbyblock.presentation.equipment.EquipmentScreen
import com.dahee.blockbyblock.presentation.equipment.EquipmentViewModel
import com.dahee.blockbyblock.presentation.home.HomeScreen
import com.dahee.blockbyblock.presentation.me.MeScreen
import com.dahee.blockbyblock.presentation.navigation.AppBottomNav
import com.dahee.blockbyblock.presentation.navigation.NavTab

@Composable
fun App() {
    val equipmentRepository = remember { InMemoryEquipmentRepository() }
    val equipmentViewModel = remember { EquipmentViewModel(equipmentRepository) }

    var currentTab by remember { mutableStateOf(NavTab.EQUIPMENT) }
    var currentLanguage by remember { mutableStateOf(AppLanguage.KO) }

    CompositionLocalProvider(
        LocalAppLanguage provides currentLanguage,
        LocalStrings provides getStrings(currentLanguage)
    ) {
        val strings = LocalStrings.current

        BlockByBlockTheme {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.Background)
                    .safeDrawingPadding()
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    when (currentTab) {
                        NavTab.TODAY -> HomeScreen(
                            viewModel = equipmentViewModel,
                            onNavigateToEquipment = { currentTab = NavTab.EQUIPMENT }
                        )
                        NavTab.EQUIPMENT -> EquipmentScreen(
                            viewModel = equipmentViewModel
                        )
                        NavTab.INVENTORY -> PlaceholderTabScreen(
                            title = strings.tabInventoryNav,
                            desc = strings.homeInventorySubtitle
                        )
                        NavTab.MEAL_PLAN -> PlaceholderTabScreen(
                            title = strings.tabMealPlan,
                            desc = strings.homeTodayMealSubtitle
                        )
                        NavTab.ME -> MeScreen(
                            onLanguageChange = { currentLanguage = it }
                        )
                    }
                }

                AppBottomNav(
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it }
                )
            }
        }
    }
}

@Composable
private fun PlaceholderTabScreen(title: String, desc: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "📦",
                fontSize = 40.sp
            )
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary,
                modifier = Modifier.padding(top = 12.dp)
            )
            Text(
                text = desc,
                fontSize = 13.sp,
                color = AppColors.TextSecondary,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}