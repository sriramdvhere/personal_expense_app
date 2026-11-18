package com.personal.expensemanager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.personal.expensemanager.data.local.entity.ExpenseEntity
import com.personal.expensemanager.model.Category

data class CategoryTotal(val category: Category, val total: Double)

@Dao
interface ExpenseDao {

    @Insert
    suspend fun insert(expense: ExpenseEntity): Long

    @Insert
    suspend fun insertAll(expenses: List<ExpenseEntity>)

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Delete
    suspend fun delete(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    suspend fun getAllExpenses(): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE date >= :startMillis AND date <= :endMillis ORDER BY date DESC")
    suspend fun getExpensesByDateRange(startMillis: Long, endMillis: Long): List<ExpenseEntity>

    @Query("SELECT SUM(amount) FROM expenses WHERE date >= :startMillis AND date <= :endMillis")
    suspend fun getTotalByDateRange(startMillis: Long, endMillis: Long): Double?

    @Query("SELECT category, SUM(amount) AS total FROM expenses WHERE date >= :startMillis AND date <= :endMillis GROUP BY category")
    suspend fun getCategoryBreakdown(startMillis: Long, endMillis: Long): List<CategoryTotal>

    @Query("SELECT * FROM expenses ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentExpenses(limit: Int): List<ExpenseEntity>
}
