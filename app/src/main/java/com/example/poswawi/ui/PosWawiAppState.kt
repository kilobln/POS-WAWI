package com.example.poswawi.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.poswawi.R

enum class PosDestination(val route: String, @StringRes val label: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    POS("pos", R.string.nav_pos, Icons.Filled.PointOfSale),
    INVENTORY("inventory", R.string.nav_inventory, Icons.Filled.Inventory2),
    EMPLOYEES("employees", R.string.nav_employees, Icons.Filled.Group),
    REPORTS("reports", R.string.nav_reports, Icons.Filled.Analytics),
    SETTINGS("settings", R.string.nav_settings, Icons.Filled.Settings)
}

@Stable
class PosWawiAppState(val navController: NavHostController) {
    val destinations: List<PosDestination> = PosDestination.entries.toList()

    fun navigateTo(destination: PosDestination) {
        navController.navigate(destination.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun isCurrentDestination(destination: PosDestination): Boolean {
        return navController.currentDestination?.route == destination.route
    }
}

@Composable
fun rememberPosWawiAppState(
    navController: NavHostController = rememberNavController()
): PosWawiAppState = remember(navController) { PosWawiAppState(navController) }
