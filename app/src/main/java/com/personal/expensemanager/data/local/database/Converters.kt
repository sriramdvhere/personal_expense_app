package com.personal.expensemanager.data.local.database

import androidx.room.TypeConverter
import com.personal.expensemanager.model.Category

class Converters {

    @TypeConverter
    fun fromCategory(category: Category): String = category.name

    @TypeConverter
    fun toCategory(value: String): Category = Category.valueOf(value)
}
