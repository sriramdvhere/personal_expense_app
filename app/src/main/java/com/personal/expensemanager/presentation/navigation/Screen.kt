package com.personal.expensemanager.presentation.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object AddExpense : Screen("add_expense")
    object EditExpense : Screen("edit_expense/{expenseId}") {
        fun createRoute(expenseId: Long): String = "edit_expense/$expenseId"
    }
    object ExpenseList : Screen("expense_list")
    object ImportExport : Screen("import_export")
}
