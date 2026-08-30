package com.dahee.blockbyblock.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavTab(
    val title: String,
    val iconVector: ImageVector
) {
    MEAL_PLAN("식단", Icons.Default.DateRange),
    BLOCK("블록", Icons.Default.Widgets),
    INVENTORY("보관함", Icons.Default.ShoppingCart),
    ME("MY", Icons.Default.Person)
}
