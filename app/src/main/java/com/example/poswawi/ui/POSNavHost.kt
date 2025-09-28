package com.example.poswawi.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.poswawi.ui.screens.EmployeeScreen
import com.example.poswawi.ui.screens.InventoryScreen
import com.example.poswawi.ui.screens.PosScreen
import com.example.poswawi.ui.screens.ReportsScreen
import com.example.poswawi.ui.screens.SettingsScreen

@Composable
fun POSNavHost(appState: PosWawiAppState) {
    val navBackStackEntry by appState.navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                appState.destinations.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = { appState.navigateTo(destination) },
                        icon = { androidx.compose.material3.Icon(destination.icon, contentDescription = null) },
                        label = { Text(text = appState.navController.context.getString(destination.label)) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = appState.navController,
            startDestination = PosDestination.POS.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(PosDestination.POS.route) {
                PosScreen()
            }
            composable(PosDestination.INVENTORY.route) {
                InventoryScreen()
            }
            composable(PosDestination.EMPLOYEES.route) {
                EmployeeScreen()
            }
            composable(PosDestination.REPORTS.route) {
                ReportsScreen()
            }
            composable(PosDestination.SETTINGS.route) {
                SettingsScreen()
            }
        }
    }
}
