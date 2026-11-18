package com.personal.expensemanager.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class DateUtilsTest {

    @Test
    fun epochMillisRoundTripReturnsSameLocalDate() {
        val original = LocalDate.of(2025, 3, 25)
        val millis = original.toEpochMillis()
        val restored = millis.toLocalDate()
        assertEquals(original, restored)
    }

    @Test
    fun formatDisplayDateFormatsKnownDateCorrectly() {
        val date = LocalDate.of(2025, 3, 25)
        val formatted = formatDisplayDate(date)
        assertEquals("25/03/2025", formatted)
    }

    @Test
    fun parseDateParsesValidStringCorrectly() {
        val parsed = parseDate("25/03/2025")
        assertEquals(LocalDate.of(2025, 3, 25), parsed)
    }

    @Test
    fun parseDateReturnsNullForInvalidString() {
        val parsed = parseDate("not-a-date")
        assertNull(parsed)
    }
}
