package com.personal.expensemanager.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.personal.expensemanager.data.local.dao.ExpenseDao
import com.personal.expensemanager.data.local.database.ExpenseDatabase
import com.personal.expensemanager.data.local.entity.ExpenseEntity
import com.personal.expensemanager.model.Category
import com.personal.expensemanager.util.toEpochMillis
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class ExpenseDaoTest {

    private lateinit var db: ExpenseDatabase
    private lateinit var dao: ExpenseDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ExpenseDatabase::class.java
        ).build()
        dao = db.expenseDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun entity(
        date: Long,
        amount: Double,
        category: Category,
        note: String? = null,
        now: Long = System.currentTimeMillis()
    ) = ExpenseEntity(
        date = date,
        amount = amount,
        category = category,
        note = note,
        createdAt = now,
        updatedAt = now
    )

    // 11.5 — CRUD

    @Test
    fun insert_then_getAllExpenses_returnsInsertedRow() = runTest {
        val expense = entity(
            date = LocalDate.of(2025, 3, 25).toEpochMillis(),
            amount = 250.0,
            category = Category.FOOD_DINING,
            note = "Swiggy lunch"
        )
        dao.insert(expense)

        val all = dao.getAllExpenses()
        assertEquals(1, all.size)
        assertEquals(250.0, all[0].amount, 0.0)
        assertEquals(Category.FOOD_DINING, all[0].category)
        assertEquals("Swiggy lunch", all[0].note)
    }

    @Test
    fun update_expense_getAllExpenses_returnsUpdatedAmount() = runTest {
        val dateMillis = LocalDate.of(2025, 3, 25).toEpochMillis()
        val id = dao.insert(
            entity(date = dateMillis, amount = 250.0, category = Category.FOOD_DINING)
        )

        val inserted = dao.getAllExpenses().first { it.id == id }
        dao.update(inserted.copy(amount = 500.0))

        val all = dao.getAllExpenses()
        assertEquals(1, all.size)
        assertEquals(500.0, all[0].amount, 0.0)
    }

    @Test
    fun delete_expense_getAllExpenses_returnsEmptyList() = runTest {
        val id = dao.insert(
            entity(
                date = LocalDate.of(2025, 3, 25).toEpochMillis(),
                amount = 250.0,
                category = Category.FOOD_DINING
            )
        )

        val inserted = dao.getAllExpenses().first { it.id == id }
        dao.delete(inserted)

        assertTrue(dao.getAllExpenses().isEmpty())
    }

    // 11.6 — Date range query

    @Test
    fun getExpensesByDateRange_returnsOnlyExpensesWithinRange() = runTest {
        val day1 = LocalDate.of(2025, 3, 23).toEpochMillis()
        val day2 = LocalDate.of(2025, 3, 24).toEpochMillis()
        val day3 = LocalDate.of(2025, 3, 25).toEpochMillis()

        dao.insertAll(
            listOf(
                entity(date = day1, amount = 100.0, category = Category.GROCERIES),
                entity(date = day2, amount = 200.0, category = Category.TRANSPORTATION),
                entity(date = day3, amount = 300.0, category = Category.SHOPPING)
            )
        )

        val result = dao.getExpensesByDateRange(day2, day3)
        assertEquals(2, result.size)
        assertTrue(result.all { it.date in day2..day3 })
    }

    @Test
    fun getExpensesByDateRange_withRangeCoveringNone_returnsEmptyList() = runTest {
        val day1 = LocalDate.of(2025, 3, 23).toEpochMillis()
        val day2 = LocalDate.of(2025, 3, 24).toEpochMillis()
        dao.insertAll(
            listOf(
                entity(date = day1, amount = 100.0, category = Category.GROCERIES),
                entity(date = day2, amount = 200.0, category = Category.TRANSPORTATION)
            )
        )

        val outOfRange = dao.getExpensesByDateRange(
            LocalDate.of(2025, 4, 1).toEpochMillis(),
            LocalDate.of(2025, 4, 30).toEpochMillis()
        )
        assertTrue(outOfRange.isEmpty())
    }

    // 11.7 — No unique constraint: identical rows are both stored

    @Test
    fun insertDuplicateRows_getAllExpenses_returnsBothRows() = runTest {
        val dateMillis = LocalDate.of(2025, 3, 25).toEpochMillis()
        val duplicate = entity(
            date = dateMillis,
            amount = 150.0,
            category = Category.TRANSPORTATION,
            note = "Ola cab"
        )
        dao.insert(duplicate)
        dao.insert(duplicate.copy(id = 0))

        val all = dao.getAllExpenses()
        assertEquals(2, all.size)
        assertTrue(all.all { it.amount == 150.0 && it.category == Category.TRANSPORTATION && it.note == "Ola cab" })
    }

    // 11.8 — Category breakdown totals

    @Test
    fun getCategoryBreakdown_returnsCorrectTotalsPerCategory() = runTest {
        val dateMillis = LocalDate.of(2025, 3, 15).toEpochMillis()
        dao.insertAll(
            listOf(
                entity(date = dateMillis, amount = 300.0, category = Category.FOOD_DINING),
                entity(date = dateMillis, amount = 200.0, category = Category.FOOD_DINING),
                entity(date = dateMillis, amount = 1500.0, category = Category.RENT),
                entity(date = dateMillis, amount = 100.0, category = Category.TRANSPORTATION)
            )
        )

        val start = LocalDate.of(2025, 3, 1).toEpochMillis()
        val end = LocalDate.of(2025, 3, 31).toEpochMillis()
        val breakdown = dao.getCategoryBreakdown(start, end)

        val totals = breakdown.associate { it.category to it.total }
        assertEquals(500.0, totals[Category.FOOD_DINING]!!, 0.0)
        assertEquals(1500.0, totals[Category.RENT]!!, 0.0)
        assertEquals(100.0, totals[Category.TRANSPORTATION]!!, 0.0)
    }

    @Test
    fun getCategoryBreakdown_categoriesWithNoExpensesInRange_areAbsentFromResult() = runTest {
        val inRange = LocalDate.of(2025, 3, 15).toEpochMillis()
        val outOfRange = LocalDate.of(2025, 4, 15).toEpochMillis()

        dao.insertAll(
            listOf(
                entity(date = inRange, amount = 500.0, category = Category.GROCERIES),
                entity(date = outOfRange, amount = 999.0, category = Category.ENTERTAINMENT)
            )
        )

        val start = LocalDate.of(2025, 3, 1).toEpochMillis()
        val end = LocalDate.of(2025, 3, 31).toEpochMillis()
        val breakdown = dao.getCategoryBreakdown(start, end)

        assertEquals(1, breakdown.size)
        assertEquals(Category.GROCERIES, breakdown[0].category)
        assertTrue(breakdown.none { it.category == Category.ENTERTAINMENT })
    }
}
