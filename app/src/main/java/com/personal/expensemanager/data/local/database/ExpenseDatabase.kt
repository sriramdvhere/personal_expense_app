package com.personal.expensemanager.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.personal.expensemanager.data.local.dao.ExpenseDao
import com.personal.expensemanager.data.local.entity.ExpenseEntity

@Database(entities = [ExpenseEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ExpenseDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao

    companion object {
        fun create(context: Context): ExpenseDatabase =
            Room.databaseBuilder(context, ExpenseDatabase::class.java, "expenses.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
