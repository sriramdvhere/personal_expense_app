package com.personal.expensemanager.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.personal.expensemanager.presentation.screens.addexpense.AddExpenseScreen
import com.personal.expensemanager.presentation.screens.dashboard.DashboardScreen
import com.personal.expensemanager.presentation.screens.expenselist.ExpenseListScreen
import com.personal.expensemanager.presentation.screens.importexport.ImportExportScreen

@Composable
fun NavGraph(navController: NavHostController) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentRoute = currentRoute,
                onTabSelected = { screen ->
                    navController.navigate(screen.route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(Screen.Dashboard.route) { saveState = true }
                    }
                }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNavigateToAdd = { navController.navigate(Screen.AddExpense.route) },
                    onNavigateToList = { navController.navigate(Screen.ExpenseList.route) }
                )
            }
            composable(Screen.AddExpense.route) {
                AddExpenseScreen(
                    expenseId = null,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.EditExpense.route,
                arguments = listOf(navArgument("expenseId") { type = NavType.LongType })
            ) { backStackEntry ->
                AddExpenseScreen(
                    expenseId = backStackEntry.arguments?.getLong("expenseId"),
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.ExpenseList.route) {
                ExpenseListScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToEdit = { id -> navController.navigate(Screen.EditExpense.createRoute(id)) }
                )
            }
            composable(Screen.ImportExport.route) {
                ImportExportScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
