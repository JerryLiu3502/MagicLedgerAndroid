package com.jerry.magicledger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jerry.magicledger.data.TransactionType
import com.jerry.magicledger.data.db.CategoryEntity
import com.jerry.magicledger.data.repo.LedgerRepository
import com.jerry.magicledger.data.repo.MonthlySummary
import com.jerry.magicledger.data.repo.TransactionItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class TransactionDayGroup(
    val dateLabel: String,
    val income: Double,
    val expense: Double,
    val items: List<TransactionItem>,
)

data class HomeUiState(
    val amountInput: String = "",
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val selectedCategoryId: Long? = null,
    val categories: List<CategoryEntity> = emptyList(),
    val noteInput: String = "",
    val dateInput: String = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now()),
    val monthLabel: String = "",
    val summary: MonthlySummary = MonthlySummary(0.0, 0.0),
    val budgetAmount: Double? = null,
    val budgetWarningText: String = "",
    val groupedTransactions: List<TransactionDayGroup> = emptyList(),
    val infoText: String? = null,
)

class HomeViewModel(
    private val repository: LedgerRepository,
) : ViewModel() {
    private val selectedMonth = MutableStateFlow(YearMonth.now())
    private val amountInput = MutableStateFlow("")
    private val selectedType = MutableStateFlow(TransactionType.EXPENSE)
    private val selectedCategoryId = MutableStateFlow<Long?>(null)
    private val noteInput = MutableStateFlow("")
    private val dateInput = MutableStateFlow(DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now()))
    private val infoText = MutableStateFlow<String?>(null)

    private val categoryFlow = selectedType.flatMapLatest { type ->
        repository.observeCategoriesByType(type)
    }

    private val summaryFlow = selectedMonth.flatMapLatest { month ->
        repository.observeMonthlySummary(month)
    }

    private val budgetFlow = selectedMonth.flatMapLatest { month ->
        repository.observeBudget(month)
    }

    private val transactionFlow = repository.observeTransactions()

    val uiState: StateFlow<HomeUiState> = combine(
        amountInput,
        selectedType,
        selectedCategoryId,
        noteInput,
        dateInput,
        infoText,
        categoryFlow,
        transactionFlow,
        summaryFlow,
        budgetFlow,
        selectedMonth,
    ) { vals ->
        val amount = vals[0] as String
        val type = vals[1] as TransactionType
        val categoryId = vals[2] as Long?
        val note = vals[3] as String
        val date = vals[4] as String
        val info = vals[5] as String?
        @Suppress("UNCHECKED_CAST")
        val categories = vals[6] as List<CategoryEntity>
        @Suppress("UNCHECKED_CAST")
        val transactions = vals[7] as List<TransactionItem>
        val summary = vals[8] as MonthlySummary
        val budget = vals[9] as Double?
        val month = vals[10] as YearMonth

        val resolvedCategoryId = categoryId?.takeIf { id -> categories.any { it.id == id } }
            ?: categories.firstOrNull()?.id

        val budgetWarning = when {
            budget == null -> "本月预算未设置"
            summary.expense > budget -> "⚠ 已超预算：${(summary.expense - budget).toInt()} 元"
            else -> "预算剩余：${(budget - summary.expense).toInt()} 元"
        }

        HomeUiState(
            amountInput = amount,
            selectedType = type,
            selectedCategoryId = resolvedCategoryId,
            categories = categories,
            noteInput = note,
            dateInput = date,
            monthLabel = "${month.year}-${month.monthValue.toString().padStart(2, '0')}",
            summary = summary,
            budgetAmount = budget,
            budgetWarningText = budgetWarning,
            groupedTransactions = transactions.toGroupedByDay(),
            infoText = info,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    init {
        viewModelScope.launch {
            repository.seedIfNeeded()
        }
    }

    fun onAmountChange(value: String) {
        amountInput.value = value
    }

    fun onTypeChange(type: TransactionType) {
        selectedType.value = type
    }

    fun onCategorySelect(categoryId: Long) {
        selectedCategoryId.value = categoryId
    }

    fun onNoteChange(value: String) {
        noteInput.value = value
    }

    fun onDateChange(value: String) {
        dateInput.value = value
    }

    fun dismissInfo() {
        infoText.value = null
    }

    fun addTransaction() {
        viewModelScope.launch {
            val amount = amountInput.value.toDoubleOrNull()
            if (amount == null || amount <= 0.0) {
                infoText.value = "请输入有效金额"
                return@launch
            }
            val categoryId = uiState.value.selectedCategoryId
            if (categoryId == null) {
                infoText.value = "请先创建分类"
                return@launch
            }
            val date = runCatching {
                LocalDate.parse(dateInput.value, DateTimeFormatter.ISO_LOCAL_DATE)
            }.getOrElse {
                infoText.value = "日期格式应为 yyyy-MM-dd"
                return@launch
            }

            repository.addTransaction(
                amount = amount,
                type = selectedType.value,
                categoryId = categoryId,
                note = noteInput.value.trim(),
                dateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            )
            amountInput.value = ""
            noteInput.value = ""
            infoText.value = "记账成功"
        }
    }

    companion object {
        fun factory(repository: LedgerRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(repository) as T
                }
            }
        }
    }
}

private fun List<TransactionItem>.toGroupedByDay(): List<TransactionDayGroup> {
    val formatter = DateTimeFormatter.ofPattern("MM月dd日")
    val zone = ZoneId.systemDefault()
    return groupBy { item ->
        Instant.ofEpochMilli(item.dateMillis).atZone(zone).toLocalDate()
    }.map { (date, items) ->
        val income = items.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = items.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        TransactionDayGroup(
            dateLabel = formatter.format(date),
            income = income,
            expense = expense,
            items = items,
        )
    }
}
