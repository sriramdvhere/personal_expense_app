package com.personal.expensemanager.model

import java.time.YearMonth

data class MonthlySummary(
    val yearMonth: YearMonth,
    val total: Double
)
