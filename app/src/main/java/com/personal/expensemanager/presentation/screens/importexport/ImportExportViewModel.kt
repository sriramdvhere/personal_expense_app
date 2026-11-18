package com.personal.expensemanager.presentation.screens.importexport

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.personal.expensemanager.data.local.entity.ExpenseEntity
import com.personal.expensemanager.data.repository.ExpenseRepository
import com.personal.expensemanager.util.buildShareIntent
import com.personal.expensemanager.util.parseCsv
import com.personal.expensemanager.util.saveCsvToDownloads
import com.personal.expensemanager.util.toEpochMillis
import com.personal.expensemanager.util.writeCsv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.time.LocalDate

class ImportExportViewModel(
    private val repository: ExpenseRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ImportExportState())
    val uiState: StateFlow<ImportExportState> = _uiState.asStateFlow()

    fun updateExportStartDate(date: LocalDate) {
        _uiState.update { it.copy(exportStartDate = date, exportAllData = false) }
    }

    fun updateExportEndDate(date: LocalDate) {
        _uiState.update { it.copy(exportEndDate = date, exportAllData = false) }
    }

    fun toggleExportAllData(checked: Boolean) {
        _uiState.update {
            it.copy(
                exportAllData = checked,
                exportStartDate = if (checked) null else it.exportStartDate,
                exportEndDate = if (checked) null else it.exportEndDate
            )
        }
    }

    fun importFromCsv(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true) }
            try {
                val result = withContext(Dispatchers.IO) {
                    val inputStream = getApplication<Application>().contentResolver
                        .openInputStream(uri)
                        ?: return@withContext ImportResult(
                            totalRecords = 0,
                            importedCount = 0,
                            skippedCount = 0,
                            errorCount = 1,
                            errors = listOf("Failed to open file")
                        )

                    val parseResult = parseCsv(inputStream)
                    val existingExpenses = repository.getAllExpenses()
                    val existingKeys = existingExpenses.mapTo(HashSet()) { e ->
                        "${e.date}|${e.amount}|${e.category.name}|${e.note}"
                    }

                    var importedCount = 0
                    var skippedCount = 0
                    val toInsert = mutableListOf<ExpenseEntity>()
                    val now = System.currentTimeMillis()

                    for (row in parseResult.parsedRows) {
                        val dateMillis = row.date.toEpochMillis()
                        val key = "$dateMillis|${row.amount}|${row.category.name}|${row.note}"
                        if (key in existingKeys) {
                            skippedCount++
                        } else {
                            toInsert.add(
                                ExpenseEntity(
                                    date = dateMillis,
                                    amount = row.amount,
                                    category = row.category,
                                    note = row.note,
                                    createdAt = now,
                                    updatedAt = now
                                )
                            )
                            // Track within-batch duplicates by adding to the set immediately
                            existingKeys.add(key)
                            importedCount++
                        }
                    }

                    if (toInsert.isNotEmpty()) {
                        repository.insertAll(toInsert)
                    }

                    ImportResult(
                        totalRecords = parseResult.parsedRows.size + parseResult.errors.size,
                        importedCount = importedCount,
                        skippedCount = skippedCount,
                        errorCount = parseResult.errors.size,
                        errors = parseResult.errors
                    )
                }
                _uiState.update { it.copy(isImporting = false, importResult = result) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isImporting = false, error = e.message) }
            }
        }
    }

    fun exportToCsv(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            try {
                val state = _uiState.value
                val (result, shareUri) = withContext(Dispatchers.IO) {
                    val expenses = when {
                        state.exportAllData -> repository.getAllExpenses()
                        state.exportStartDate != null || state.exportEndDate != null -> {
                            val startMillis = state.exportStartDate?.toEpochMillis() ?: Long.MIN_VALUE
                            val endMillis = state.exportEndDate?.toEpochMillis() ?: Long.MAX_VALUE
                            repository.getExpensesByDateRange(startMillis, endMillis)
                        }
                        else -> repository.getAllExpenses()
                    }

                    val byteStream = ByteArrayOutputStream()
                    writeCsv(expenses, byteStream)
                    val content = byteStream.toString(Charsets.UTF_8.name())
                    val uri = saveCsvToDownloads(context, content)

                    if (uri == null) {
                        return@withContext Pair(
                            ExportResult(success = false, fileName = null, recordCount = 0),
                            null as Uri?
                        )
                    }

                    val fileName = context.contentResolver.query(
                        uri,
                        arrayOf(MediaStore.Downloads.DISPLAY_NAME),
                        null, null, null
                    )?.use { cursor ->
                        if (cursor.moveToFirst()) cursor.getString(0) else null
                    }

                    Pair(
                        ExportResult(success = true, fileName = fileName, recordCount = expenses.size),
                        uri
                    )
                }

                if (shareUri != null) {
                    context.startActivity(
                        buildShareIntent(shareUri).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    )
                }
                _uiState.update { it.copy(isExporting = false, exportResult = result) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isExporting = false, error = e.message) }
            }
        }
    }

    fun dismissImportResult() {
        _uiState.update { it.copy(importResult = null) }
    }

    fun dismissExportResult() {
        _uiState.update { it.copy(exportResult = null) }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
        fun factory(
            repository: ExpenseRepository,
            application: Application
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(klass: Class<T>): T =
                ImportExportViewModel(repository, application) as T
        }
    }
}
