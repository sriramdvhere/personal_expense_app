package com.personal.expensemanager.util

import com.personal.expensemanager.model.Category
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DuplicateDetectionTest {

    private fun buildKey(
        date: LocalDate,
        amount: Double,
        category: Category,
        note: String?
    ): String = "${date.toEpochMillis()}|$amount|${category.name}|$note"

    private fun makeSet(vararg keys: String): HashSet<String> = hashSetOf(*keys)

    @Test
    fun sameKeyIsIdentifiedAsDuplicate() {
        val date = LocalDate.of(2025, 3, 25)
        val key = buildKey(date, 250.0, Category.FOOD_DINING, "Swiggy lunch")
        val existingKeys = makeSet(key)

        assertTrue(existingKeys.contains(buildKey(date, 250.0, Category.FOOD_DINING, "Swiggy lunch")))
    }

    @Test
    fun sameAmountOnDifferentDateIsNotDuplicate() {
        val date1 = LocalDate.of(2025, 3, 25)
        val date2 = LocalDate.of(2025, 3, 26)
        val existingKeys = makeSet(buildKey(date1, 250.0, Category.FOOD_DINING, "Swiggy lunch"))

        assertFalse(existingKeys.contains(buildKey(date2, 250.0, Category.FOOD_DINING, "Swiggy lunch")))
    }

    @Test
    fun sameDateAndAmountWithDifferentCategoryIsNotDuplicate() {
        val date = LocalDate.of(2025, 3, 25)
        val existingKeys = makeSet(buildKey(date, 250.0, Category.FOOD_DINING, "note"))

        assertFalse(existingKeys.contains(buildKey(date, 250.0, Category.SHOPPING, "note")))
    }

    @Test
    fun sameDateAmountCategoryWithDifferentNoteIsNotDuplicate() {
        val date = LocalDate.of(2025, 3, 25)
        val existingKeys = makeSet(buildKey(date, 250.0, Category.FOOD_DINING, "Swiggy lunch"))

        assertFalse(existingKeys.contains(buildKey(date, 250.0, Category.FOOD_DINING, "Zomato lunch")))
    }

    @Test
    fun nullNoteAndEmptyStringNoteProduceDifferentKeys() {
        val date = LocalDate.of(2025, 3, 25)
        val keyWithNull = buildKey(date, 100.0, Category.GROCERIES, null)
        val keyWithEmpty = buildKey(date, 100.0, Category.GROCERIES, "")

        assertFalse(keyWithNull == keyWithEmpty)

        val existingKeys = makeSet(keyWithNull)
        assertFalse(existingKeys.contains(keyWithEmpty))
    }
}
