package com.autodiag.ai.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.autodiag.ai.ui.screens.bluetooth.BluetoothScreen
import com.autodiag.ai.ui.screens.history.HistoryScreen
import com.autodiag.ai.ui.screens.home.HomeScreen
import com.autodiag.ai.ui.screens.settings.SettingsScreen
import com.autodiag.ai.presentation.screens.analysis.DataCollectionScreen
import com.autodiag.ai.presentation.screens.analysis.AnalysisResultsScreen

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : Screen(
        route = "home",
        title = "Главная",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )
    
    object Bluetooth : Screen(
        route = "bluetooth",
        title = "OBD2",
        selectedIcon = Icons.Filled.Bluetooth,
        unselectedIcon = Icons.Outlined.Bluetooth
    )
    
    object History : Screen(
        route = "history",
        title = "История",
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History
    )
    
    object Settings : Screen(
        route = "settings",
        title = "Настройки",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )

    object Analysis : Screen(
        route = "analysis",
        title = "Анализ",
        selectedIcon = Icons.Filled.Analytics,
        unselectedIcon = Icons.Outlined.Analytics
    )

    object AnalysisResults : Screen(
        route = "analysis_results",
        title = "Результаты",
        selectedIcon = Icons.Filled.Assessment,
        unselectedIcon = Icons.Outlined.Assessment
    )
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Bluetooth,
    Screen.Analysis,
    Screen.History,
    Screen.Settings
)

@Composable
fun AutoDiagNavHost(
    navController: NavHostController = rememberNavController()
) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen()
            }
            composable(Screen.Bluetooth.route) {
                BluetoothScreen()
            }
            composable(Screen.History.route) {
                HistoryScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
            composable(Screen.Analysis.route) {
                DataCollectionScreen(
                    onNavigateToResults = {
                        navController.navigate(Screen.AnalysisResults.route)
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.AnalysisResults.route) {
                AnalysisResultsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSettingsApplied = {
                        navController.popBackStack(Screen.Home.route, false)
                    }
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    NavigationBar {
        bottomNavItems.forEach { screen ->
            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
            
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                        contentDescription = screen.title
                    )
                },
                label = { Text(screen.title) },
                selected = selected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
