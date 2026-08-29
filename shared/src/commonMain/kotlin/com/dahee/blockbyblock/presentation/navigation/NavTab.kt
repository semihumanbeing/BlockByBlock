package com.dahee.blockbyblock.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavTab(
    val title: String,
    val iconVector: ImageVector
) {
    TODAY("오늘", Icons.Default.Home),
    INVENTORY("보관함", Icons.Default.ShoppingCart),
    MEAL_PLAN("식단", Icons.Default.DateRange),
    EQUIPMENT("장비", Icons.Default.Settings),
    ME("MY", Icons.Default.Person)
}
