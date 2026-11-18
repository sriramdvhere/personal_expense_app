package com.personal.expensemanager.util

import java.text.NumberFormat
import java.util.Locale

private val INR_FORMAT: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

fun formatAmount(amount: Double): String = INR_FORMAT.format(amount)
