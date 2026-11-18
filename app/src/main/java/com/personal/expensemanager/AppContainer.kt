package com.personal.expensemanager

import android.content.Context
import com.personal.expensemanager.data.local.database.ExpenseDatabase
import com.personal.expensemanager.data.repository.ExpenseRepository
import com.personal.expensemanager.data.repository.ExpenseRepositoryImpl

class AppContainer(context: Context) {
    val database: ExpenseDatabase = ExpenseDatabase.create(context)
    val repository: ExpenseRepository = ExpenseRepositoryImpl(database.expenseDao())
}
