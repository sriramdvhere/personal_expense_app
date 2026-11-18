package com.personal.expensemanager.util

import com.personal.expensemanager.data.local.entity.ExpenseEntity
import com.personal.expensemanager.model.Category
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.time.LocalDate

data class CsvRow(
    val date: LocalDate,
    val amount: Double,
    val category: Category,
    val note: String?
)

data class CsvParseResult(
    val parsedRows: List<CsvRow>,
    val errors: List<String>
)

fun parseCsv(inputStream: InputStream): CsvParseResult {
    val rows = mutableListOf<CsvRow>()
    val errors = mutableListOf<String>()

    BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
        var lineNumber = 0
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            lineNumber++
            if (lineNumber == 1) continue

            val parts = line!!.split(",", limit = 4)
            if (parts.size < 3) {
                errors.add("Row $lineNumber: insufficient columns")
                continue
            }

            val dateStr = parts[0].trim()
            val amountStr = parts[1].trim()
            val categoryStr = parts[2].trim()
            val noteStr = if (parts.size >= 4) parts[3].trim().ifEmpty { null } else null

            val date = parseDate(dateStr)
            if (date == null) {
                errors.add("Row $lineNumber: invalid date '$dateStr'")
                continue
            }

            val amount = amountStr.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                errors.add("Row $lineNumber: invalid amount '$amountStr'")
                continue
            }

            val category = Category.entries.firstOrNull {
                it.displayName.lowercase() == categoryStr.lowercase()
            } ?: Category.OTHERS

            rows.add(CsvRow(date, amount, category, noteStr))
        }
    }

    return CsvParseResult(rows, errors)
}

fun writeCsv(expenses: List<ExpenseEntity>, outputStream: OutputStream) {
    BufferedWriter(OutputStreamWriter(outputStream, Charsets.UTF_8)).use { writer ->
        writer.write(Constants.CSV_HEADER)
        writer.newLine()
        for (expense in expenses) {
            val dateStr = formatDisplayDate(expense.date.toLocalDate())
            val amountStr = "%.2f".format(expense.amount)
            val noteStr = expense.note ?: ""
            writer.write("$dateStr,$amountStr,${expense.category.displayName},$noteStr")
            writer.newLine()
        }
    }
}
