package com.personal.expensemanager.presentation.screens.addexpense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.personal.expensemanager.data.local.entity.ExpenseEntity
import com.personal.expensemanager.data.repository.ExpenseRepository
import com.personal.expensemanager.model.Category
import com.personal.expensemanager.util.toEpochMillis
import com.personal.expensemanager.util.toLocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class AddExpenseViewModel(
    private val repository: ExpenseRepository,
    private val expenseId: Long?
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddExpenseState(expenseId = expenseId))
    val uiState: StateFlow<AddExpenseState> = _uiState.asStateFlow()

    private var originalCreatedAt: Long? = null

    init {
        if (expenseId != null) {
            loadExpense(expenseId)
        }
    }

    private fun loadExpense(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val expense = withContext(Dispatchers.IO) {
                    repository.getAllExpenses().firstOrNull { it.id == id }
                }
                if (expense != null) {
                    originalCreatedAt = expense.createdAt
                    _uiState.update {
                        it.copy(
                            date = expense.date.toLocalDate(),
                            amount = expense.amount.toBigDecimal().stripTrailingZeros().toPlainString(),
                            selectedCategory = expense.category,
                            note = expense.note ?: "",
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Expense not found") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun updateDate(date: LocalDate) {
        _uiState.update { it.copy(date = date, dateError = null) }
    }

    fun updateAmount(amount: String) {
        _uiState.update { it.copy(amount = amount, amountError = null) }
    }

    fun updateCategory(category: Category) {
        _uiState.update { it.copy(selectedCategory = category, categoryError = null) }
    }

    fun updateNote(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun saveExpense() {
        val state = _uiState.value
        val dateError = validateDate(state.date)
        val amountError = validateAmount(state.amount)
        val categoryError = if (state.selectedCategory == null) "Please select a category" else null

        if (dateError != null || amountError != null || categoryError != null) {
            _uiState.update {
                it.copy(
                    dateError = dateError,
                    amountError = amountError,
                    categoryError = categoryError
                )
            }
            return
        }

        val amount = state.amount.toDouble()
        val now = System.currentTimeMillis()

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                withContext(Dispatchers.IO) {
                    if (expenseId == null) {
                        repository.insert(
                            ExpenseEntity(
                                date = state.date.toEpochMillis(),
                                amount = amount,
                                category = state.selectedCategory!!,
                                note = state.note.trim().ifEmpty { null },
                                createdAt = now,
                                updatedAt = now
                            )
                        )
                    } else {
                        repository.update(
                            ExpenseEntity(
                                id = expenseId,
                                date = state.date.toEpochMillis(),
                                amount = amount,
                                category = state.selectedCategory!!,
                                note = state.note.trim().ifEmpty { null },
                                createdAt = originalCreatedAt ?: now,
                                updatedAt = now
                            )
                        )
                    }
                }
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun validateDate(date: LocalDate): String? =
        if (date.isAfter(LocalDate.now())) "Date cannot be in the future" else null

    private fun validateAmount(amount: String): String? {
        if (amount.isBlank()) return "Amount is required"
        val parsed = amount.toDoubleOrNull() ?: return "Invalid amount"
        if (parsed <= 0) return "Amount must be greater than zero"
        val parts = amount.split(".")
        if (parts.size == 2 && parts[1].length > 2) return "Amount cannot have more than 2 decimal places"
        return null
    }

    companion object {
        fun Factory(repository: ExpenseRepository, expenseId: Long?): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    AddExpenseViewModel(repository, expenseId) as T
            }
    }
}
