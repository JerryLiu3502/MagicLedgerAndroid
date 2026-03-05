package com.jerry.magicledger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jerry.magicledger.data.TransactionType
import com.jerry.magicledger.data.repo.CategorySummaryItem
import com.jerry.magicledger.data.repo.LedgerRepository
import com.jerry.magicledger.data.repo.MonthlySummary
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth

data class CategoryStatUiItem(
    val name: String,
    val amount: Double,
    val ratio: Float,
)

data class StatsUiState(
    val monthLabel: String = "",
    val summary: MonthlySummary = MonthlySummary(0.0, 0.0),
    val expenseStats: List<CategoryStatUiItem> = emptyList(),
    val incomeStats: List<CategoryStatUiItem> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModel(
    private val repository: LedgerRepository,
) : ViewModel() {
    private val selectedMonth = MutableStateFlow(YearMonth.now())

    private val summaryFlow = selectedMonth.flatMapLatest { repository.observeMonthlySummary(it) }
    private val categorySummaryFlow = selectedMonth.flatMapLatest { repository.observeMonthlyCategorySummary(it) }

    val uiState: StateFlow<StatsUiState> = combine(
        selectedMonth,
        summaryFlow,
        categorySummaryFlow,
    ) { month, summary, categorySummary ->
        StatsUiState(
            monthLabel = "${month.year}-${month.monthValue.toString().padStart(2, '0')}",
            summary = summary,
            expenseStats = categorySummary.toUiList(TransactionType.EXPENSE),
            incomeStats = categorySummary.toUiList(TransactionType.INCOME),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())

    init {
        viewModelScope.launch {
            repository.seedIfNeeded()
        }
    }

    fun goPrevMonth() {
        selectedMonth.value = selectedMonth.value.minusMonths(1)
    }

    fun goNextMonth() {
        selectedMonth.value = selectedMonth.value.plusMonths(1)
    }

    fun goCurrentMonth() {
        selectedMonth.value = YearMonth.now()
    }

    companion object {
        fun factory(repository: LedgerRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StatsViewModel(repository) as T
                }
            }
        }
    }
}

private fun List<CategorySummaryItem>.toUiList(type: TransactionType): List<CategoryStatUiItem> {
    val filtered = filter { it.type == type && it.totalAmount > 0.0 }
    val total = filtered.sumOf { it.totalAmount }
    if (total <= 0.0) {
        return emptyList()
    }
    return filtered.map {
        CategoryStatUiItem(
            name = it.categoryName,
            amount = it.totalAmount,
            ratio = (it.totalAmount / total).toFloat(),
        )
    }
}
