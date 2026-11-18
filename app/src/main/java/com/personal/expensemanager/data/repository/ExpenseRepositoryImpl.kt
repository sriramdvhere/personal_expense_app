package com.personal.expensemanager.data.repository

import com.personal.expensemanager.data.local.dao.CategoryTotal
import com.personal.expensemanager.data.local.dao.ExpenseDao
import com.personal.expensemanager.data.local.entity.ExpenseEntity

class ExpenseRepositoryImpl(private val dao: ExpenseDao) : ExpenseRepository {

    override suspend fun insert(expense: ExpenseEntity): Long = dao.insert(expense)

    override suspend fun insertAll(expenses: List<ExpenseEntity>) = dao.insertAll(expenses)

    override suspend fun update(expense: ExpenseEntity) = dao.update(expense)

    override suspend fun delete(expense: ExpenseEntity) = dao.delete(expense)

    override suspend fun getAllExpenses(): List<ExpenseEntity> = dao.getAllExpenses()

    override suspend fun getExpensesByDateRange(startMillis: Long, endMillis: Long): List<ExpenseEntity> =
        dao.getExpensesByDateRange(startMillis, endMillis)

    override suspend fun getTotalByDateRange(startMillis: Long, endMillis: Long): Double? =
        dao.getTotalByDateRange(startMillis, endMillis)

    override suspend fun getCategoryBreakdown(startMillis: Long, endMillis: Long): List<CategoryTotal> =
        dao.getCategoryBreakdown(startMillis, endMillis)

    override suspend fun getRecentExpenses(limit: Int): List<ExpenseEntity> =
        dao.getRecentExpenses(limit)
}
