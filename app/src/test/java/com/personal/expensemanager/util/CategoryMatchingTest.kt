package com.personal.expensemanager.util

import com.personal.expensemanager.model.Category
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryMatchingTest {

    private fun matchCategory(input: String): Category =
        Category.entries.firstOrNull {
            it.displayName.lowercase() == input.lowercase()
        } ?: Category.OTHERS

    @Test
    fun exactDisplayNameMatchReturnsCorrectCategory() {
        assertEquals(Category.FOOD_DINING, matchCategory("Food & Dining"))
        assertEquals(Category.TRANSPORTATION, matchCategory("Transportation"))
        assertEquals(Category.SHOPPING, matchCategory("Shopping"))
        assertEquals(Category.BILLS_UTILITIES, matchCategory("Bills & Utilities"))
        assertEquals(Category.ENTERTAINMENT, matchCategory("Entertainment"))
        assertEquals(Category.HEALTH_MEDICAL, matchCategory("Health & Medical"))
        assertEquals(Category.EDUCATION, matchCategory("Education"))
        assertEquals(Category.GROCERIES, matchCategory("Groceries"))
        assertEquals(Category.RENT, matchCategory("Rent"))
        assertEquals(Category.TRAVEL, matchCategory("Travel"))
        assertEquals(Category.PERSONAL_CARE, matchCategory("Personal Care"))
        assertEquals(Category.GIFTS_DONATIONS, matchCategory("Gifts & Donations"))
        assertEquals(Category.INVESTMENTS, matchCategory("Investments"))
        assertEquals(Category.INSURANCE, matchCategory("Insurance"))
        assertEquals(Category.OTHERS, matchCategory("Others"))
    }

    @Test
    fun uppercaseInputMatchesCorrectly() {
        assertEquals(Category.GROCERIES, matchCategory("GROCERIES"))
        assertEquals(Category.TRAVEL, matchCategory("TRAVEL"))
    }

    @Test
    fun mixedCaseInputMatchesCorrectly() {
        assertEquals(Category.EDUCATION, matchCategory("eDuCaTiOn"))
        assertEquals(Category.RENT, matchCategory("rEnT"))
    }

    @Test
    fun unknownStringReturnsOthers() {
        assertEquals(Category.OTHERS, matchCategory("Completely Unknown"))
    }

    @Test
    fun emptyStringReturnsOthers() {
        assertEquals(Category.OTHERS, matchCategory(""))
    }
}
