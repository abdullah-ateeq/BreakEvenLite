package com.example.breakevenlite.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null
) {
    object Splash : Screen("splash", "Splash")
    object Calculator : Screen(
        "calculator",
        "Calculator",
        Icons.Filled.Calculate,
        Icons.Outlined.Calculate
    )
    object History : Screen(
        "history",
        "History",
        Icons.Filled.History,
        Icons.Outlined.History
    )
    object Settings : Screen(
        "settings",
        "Settings",
        Icons.Filled.Settings,
        Icons.Outlined.Settings
    )
    object About : Screen(
        "about",
        "About",
        Icons.Filled.Info,
        Icons.Outlined.Info
    )

    companion object {
        val bottomNavItems = listOf(Calculator, History, Settings, About)
    }
}
