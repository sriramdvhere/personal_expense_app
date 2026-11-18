package com.personal.expensemanager.util

import com.personal.expensemanager.model.Category
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CsvHelperTest {

    private fun csvStream(content: String) =
        content.trimIndent().byteInputStream(Charsets.UTF_8)

    @Test
    fun valid4ColumnRowParsesCorrectly() {
        val csv = """
            Date,Amount,Category,Notes
            25/03/2025,250.00,Food & Dining,Swiggy lunch
        """
        val result = parseCsv(csvStream(csv))

        assertEquals(1, result.parsedRows.size)
        assertTrue(result.errors.isEmpty())

        val row = result.parsedRows[0]
        assertEquals(LocalDate.of(2025, 3, 25), row.date)
        assertEquals(250.00, row.amount, 0.001)
        assertEquals(Category.FOOD_DINING, row.category)
        assertEquals("Swiggy lunch", row.note)
    }

    @Test
    fun valid3ColumnRowParsesWithNoteNull() {
        val csv = """
            Date,Amount,Category
            24/03/2025,150,Transportation
        """
        val result = parseCsv(csvStream(csv))

        assertEquals(1, result.parsedRows.size)
        assertTrue(result.errors.isEmpty())
        assertNull(result.parsedRows[0].note)
    }

    @Test
    fun rowWithInvalidDateIsSkippedAndRecordedAsError() {
        val csv = """
            Date,Amount,Category,Notes
            99/99/2025,100.00,Shopping,test
        """
        val result = parseCsv(csvStream(csv))

        assertTrue(result.parsedRows.isEmpty())
        assertEquals(1, result.errors.size)
        assertTrue(result.errors[0].contains("invalid date"))
    }

    @Test
    fun rowWithInvalidAmountIsSkippedAndRecordedAsError() {
        val csv = """
            Date,Amount,Category,Notes
            25/03/2025,abc,Shopping,test
        """
        val result = parseCsv(csvStream(csv))

        assertTrue(result.parsedRows.isEmpty())
        assertEquals(1, result.errors.size)
        assertTrue(result.errors[0].contains("invalid amount"))
    }

    @Test
    fun rowWithNegativeAmountIsSkippedAndRecordedAsError() {
        val csv = """
            Date,Amount,Category,Notes
            25/03/2025,-50.00,Shopping,test
        """
        val result = parseCsv(csvStream(csv))

        assertTrue(result.parsedRows.isEmpty())
        assertEquals(1, result.errors.size)
        assertTrue(result.errors[0].contains("invalid amount"))
    }

    @Test
    fun emptyBlankRowIsSkipped() {
        val csv = """
            Date,Amount,Category,Notes

            25/03/2025,100.00,Groceries,
        """
        val result = parseCsv(csvStream(csv))

        assertEquals(1, result.parsedRows.size)
    }

    @Test
    fun headerRowIsNotParsedAsData() {
        val csv = """
            Date,Amount,Category,Notes
        """
        val result = parseCsv(csvStream(csv))

        assertTrue(result.parsedRows.isEmpty())
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun unknownCategoryDefaultsToOthers() {
        val csv = """
            Date,Amount,Category,Notes
            25/03/2025,100.00,XYZ Unknown Category,
        """
        val result = parseCsv(csvStream(csv))

        assertEquals(1, result.parsedRows.size)
        assertEquals(Category.OTHERS, result.parsedRows[0].category)
    }

    @Test
    fun categoryMatchingIsCaseInsensitive() {
        val csv = """
            Date,Amount,Category,Notes
            25/03/2025,100.00,FOOD & DINING,Lunch
            24/03/2025,200.00,food & dining,Dinner
            23/03/2025,300.00,Food & Dining,Breakfast
        """
        val result = parseCsv(csvStream(csv))

        assertEquals(3, result.parsedRows.size)
        result.parsedRows.forEach { row ->
            assertEquals(Category.FOOD_DINING, row.category)
        }
    }
}
