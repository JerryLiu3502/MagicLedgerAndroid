package com.jerry.magicledger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jerry.magicledger.data.repo.LedgerRepository
import com.jerry.magicledger.data.repo.MonthlySummary
import com.jerry.magicledger.utils.parseAmountOrNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth

data class BudgetUiState(
    val monthLabel: String = "",
    val summary: MonthlySummary = MonthlySummary(0.0, 0.0),
    val budgetAmount: Double? = null,
    val budgetInput: String = "",
    val warningText: String = "",
    val infoText: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetViewModel(
    private val repository: LedgerRepository,
) : ViewModel() {
    private val month = MutableStateFlow(YearMonth.now())
    private val budgetInput = MutableStateFlow("")
    private val infoText = MutableStateFlow<String?>(null)

    private val summaryFlow = month.flatMapLatest { repository.observeMonthlySummary(it) }
    private val budgetFlow = month.flatMapLatest { repository.observeBudget(it) }

    val uiState: StateFlow<BudgetUiState> = combine(
        month,
        summaryFlow,
        budgetFlow,
        budgetInput,
        infoText,
    ) { currentMonth, summary, budget, input, info ->
        val warningText = when {
            budget == null -> "设置预算后可看到超支提醒"
            summary.expense > budget -> "⚠ 本月已超支 ${(summary.expense - budget).toInt()} 元"
            else -> "预算剩余 ${(budget - summary.expense).toInt()} 元"
        }
        BudgetUiState(
            monthLabel = "${currentMonth.year}-${currentMonth.monthValue.toString().padStart(2, '0')}",
            summary = summary,
            budgetAmount = budget,
            budgetInput = input,
            warningText = warningText,
            infoText = info,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BudgetUiState())

    init {
        viewModelScope.launch {
            repository.seedIfNeeded()
        }
    }

    fun onBudgetInputChange(value: String) {
        budgetInput.value = value
    }

    fun dismissInfo() {
        infoText.value = null
    }

    fun saveBudget() {
        viewModelScope.launch {
            val parsed = parseAmountOrNull(budgetInput.value)
            if (parsed == null) {
                infoText.value = "请输入有效预算金额"
                return@launch
            }
            repository.setBudget(month.value, parsed)
            infoText.value = "预算已保存"
        }
    }

    companion object {
        fun factory(repository: LedgerRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return BudgetViewModel(repository) as T
                }
            }
        }
    }
}
