package com.personal.expensemanager.presentation.screens.importexport

import java.time.LocalDate

data class ImportResult(
    val totalRecords: Int,
    val importedCount: Int,
    val skippedCount: Int,
    val errorCount: Int,
    val errors: List<String>
)

data class ExportResult(
    val success: Boolean,
    val fileName: String?,
    val recordCount: Int
)

data class ImportExportState(
    val isImporting: Boolean = false,
    val isExporting: Boolean = false,
    val importResult: ImportResult? = null,
    val exportResult: ExportResult? = null,
    val exportStartDate: LocalDate? = null,
    val exportEndDate: LocalDate? = null,
    val exportAllData: Boolean = true,
    val error: String? = null
)
