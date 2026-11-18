package com.personal.expensemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.personal.expensemanager.presentation.navigation.NavGraph
import com.personal.expensemanager.presentation.theme.ExpenseManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExpenseManagerTheme {
                NavGraph(navController = rememberNavController())
            }
        }
    }
}
