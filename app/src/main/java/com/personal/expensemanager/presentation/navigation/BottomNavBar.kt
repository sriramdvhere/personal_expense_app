package com.personal.expensemanager.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

private data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onTabSelected: (Screen) -> Unit
) {
    val items = listOf(
        BottomNavItem(Screen.Dashboard, "Home", Icons.Default.Home),
        BottomNavItem(Screen.AddExpense, "Add", Icons.Default.Add),
        BottomNavItem(Screen.ExpenseList, "List", Icons.Default.List),
        BottomNavItem(Screen.ImportExport, "Import", Icons.Default.ImportExport)
    )

    NavigationBar {
        items.forEach { item ->
            val routePrefix = item.screen.route.substringBefore("{")
            val selected = currentRoute?.startsWith(routePrefix) == true
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(item.screen) },
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(text = item.label) }
            )
        }
    }
}
