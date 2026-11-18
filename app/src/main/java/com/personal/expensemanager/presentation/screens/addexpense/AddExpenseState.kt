package com.personal.expensemanager.presentation.screens.addexpense

import com.personal.expensemanager.model.Category
import java.time.LocalDate

data class AddExpenseState(
    val expenseId: Long? = null,
    val date: LocalDate = LocalDate.now(),
    val amount: String = "",
    val selectedCategory: Category? = null,
    val note: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val dateError: String? = null,
    val amountError: String? = null,
    val categoryError: String? = null
)
