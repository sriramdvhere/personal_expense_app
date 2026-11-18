package com.personal.expensemanager.presentation.screens.addexpense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.personal.expensemanager.ExpenseManagerApp
import com.personal.expensemanager.presentation.components.AmountTextField
import com.personal.expensemanager.presentation.components.CategoryDropdown
import com.personal.expensemanager.presentation.components.DatePickerField
import com.personal.expensemanager.presentation.components.ErrorMessage
import com.personal.expensemanager.presentation.components.LoadingIndicator
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    expenseId: Long? = null,
    onBack: () -> Unit
) {
    val app = LocalContext.current.applicationContext as ExpenseManagerApp
    val viewModel: AddExpenseViewModel = viewModel(
        factory = AddExpenseViewModel.Factory(app.container.repository, expenseId)
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onBack()
        }
    }

    val noFutureDates = remember {
        val todayUtcMillis = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                utcTimeMillis <= todayUtcMillis
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (expenseId == null) "Add Expense" else "Edit Expense") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingIndicator()
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Column {
                    DatePickerField(
                        selectedDate = uiState.date,
                        onDateSelected = viewModel::updateDate,
                        selectableDates = noFutureDates,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (uiState.dateError != null) {
                        ErrorMessage(
                            message = uiState.dateError!!,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }

                AmountTextField(
                    value = uiState.amount,
                    onValueChange = viewModel::updateAmount,
                    isError = uiState.amountError != null,
                    errorMessage = uiState.amountError,
                    modifier = Modifier.fillMaxWidth()
                )

                CategoryDropdown(
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = viewModel::updateCategory,
                    isError = uiState.categoryError != null,
                    errorMessage = uiState.categoryError,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.note,
                    onValueChange = viewModel::updateNote,
                    label = { Text("Notes (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = viewModel::saveExpense,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("SAVE EXPENSE")
                }

                if (uiState.error != null) {
                    ErrorMessage(message = uiState.error!!)
                }
            }
        }
    }
}
