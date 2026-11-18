package com.personal.expensemanager.presentation.screens.importexport

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.personal.expensemanager.ExpenseManagerApp
import com.personal.expensemanager.presentation.components.DatePickerField
import com.personal.expensemanager.presentation.components.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportExportScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as ExpenseManagerApp
    val viewModel: ImportExportViewModel = viewModel(
        factory = ImportExportViewModel.factory(app.container.repository, app)
    )
    val uiState by viewModel.uiState.collectAsState()

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importFromCsv(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import / Export") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "IMPORT DATA",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedButton(
                onClick = { importLauncher.launch(arrayOf("text/*")) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("SELECT CSV FILE")
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Expected CSV Format:",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Date,Amount,Category,Notes\n25/03/2025,250.00,Food & Dining,Swiggy lunch\n24/03/2025,150,Transportation,",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "• First row must be header", style = MaterialTheme.typography.bodySmall)
                    Text(text = "• Date format: DD/MM/YYYY", style = MaterialTheme.typography.bodySmall)
                    Text(text = "• Notes column is optional", style = MaterialTheme.typography.bodySmall)
                    Text(text = "• Exact duplicates are skipped", style = MaterialTheme.typography.bodySmall)
                }
            }

            if (uiState.isImporting) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                ) {
                    LoadingIndicator()
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Text(
                text = "EXPORT DATA",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = uiState.exportAllData,
                    onCheckedChange = { viewModel.toggleExportAllData(it) }
                )
                Text(
                    text = "Export all data",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Text(
                text = "Export Range (enabled when unchecked above)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    DatePickerField(
                        selectedDate = uiState.exportStartDate,
                        onDateSelected = { viewModel.updateExportStartDate(it) },
                        label = "Start date",
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(if (uiState.exportAllData) 0.4f else 1f)
                    )
                    if (uiState.exportAllData) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .pointerInput(Unit) { detectTapGestures { } }
                        )
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    DatePickerField(
                        selectedDate = uiState.exportEndDate,
                        onDateSelected = { viewModel.updateExportEndDate(it) },
                        label = "End date",
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(if (uiState.exportAllData) 0.4f else 1f)
                    )
                    if (uiState.exportAllData) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .pointerInput(Unit) { detectTapGestures { } }
                        )
                    }
                }
            }

            Button(
                onClick = { viewModel.exportToCsv(context) },
                enabled = !uiState.isExporting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("EXPORT AS CSV")
            }

            if (uiState.isExporting) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                ) {
                    LoadingIndicator()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    uiState.importResult?.let { result ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissImportResult() },
            title = { Text("Import Complete") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Total records: ${result.totalRecords}")
                    Text("Imported: ${result.importedCount}")
                    Text("Skipped (duplicates): ${result.skippedCount}")
                    Text("Errors: ${result.errorCount}")
                    if (result.errors.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        result.errors.take(5).forEach { error ->
                            Text(
                                text = "• $error",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        if (result.errors.size > 5) {
                            Text(
                                text = "...and ${result.errors.size - 5} more",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissImportResult() }) {
                    Text("OK")
                }
            }
        )
    }

    uiState.exportResult?.let { result ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissExportResult() },
            title = { Text(if (result.success) "Export Complete" else "Export Failed") },
            text = {
                if (result.success) {
                    Text("Exported ${result.recordCount} records to:\n${result.fileName ?: "Downloads folder"}")
                } else {
                    Text("Failed to export data. Please try again.")
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissExportResult() }) {
                    Text("OK")
                }
            }
        )
    }

    uiState.error?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissError() },
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissError() }) {
                    Text("OK")
                }
            }
        )
    }
}
