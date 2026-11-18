package com.personal.expensemanager.data.repository

import com.personal.expensemanager.data.local.dao.CategoryTotal
import com.personal.expensemanager.data.local.entity.ExpenseEntity

interface ExpenseRepository {

    suspend fun insert(expense: ExpenseEntity): Long

    suspend fun insertAll(expenses: List<ExpenseEntity>)

    suspend fun update(expense: ExpenseEntity)

    suspend fun delete(expense: ExpenseEntity)

    suspend fun getAllExpenses(): List<ExpenseEntity>

    suspend fun getExpensesByDateRange(startMillis: Long, endMillis: Long): List<ExpenseEntity>

    suspend fun getTotalByDateRange(startMillis: Long, endMillis: Long): Double?

    suspend fun getCategoryBreakdown(startMillis: Long, endMillis: Long): List<CategoryTotal>

    suspend fun getRecentExpenses(limit: Int): List<ExpenseEntity>
}
