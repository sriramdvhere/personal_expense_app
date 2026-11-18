package com.personal.expensemanager.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val DISPLAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

fun LocalDate.toEpochMillis(): Long =
    atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

fun formatDisplayDate(date: LocalDate): String = date.format(DISPLAY_FORMATTER)

fun parseDate(str: String): LocalDate? = try {
    LocalDate.parse(str.trim(), DISPLAY_FORMATTER)
} catch (e: DateTimeParseException) {
    null
}
