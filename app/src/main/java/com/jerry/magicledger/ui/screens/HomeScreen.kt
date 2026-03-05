@file:OptIn(ExperimentalMaterial3Api::class)

package com.jerry.magicledger.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jerry.magicledger.data.TransactionType
import com.jerry.magicledger.data.repo.LedgerRepository
import com.jerry.magicledger.ui.components.toDateTimeText
import com.jerry.magicledger.ui.components.toMoneyText
import com.jerry.magicledger.viewmodel.HomeViewModel
import com.jerry.magicledger.viewmodel.TransactionDayGroup

@Composable
fun HomeRoute(repository: LedgerRepository) {
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory(repository))
    HomeScreen(viewModel = viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .padding(12.dp),
    ) {
        item {
            SummaryCard(
                monthLabel = uiState.monthLabel,
                income = uiState.summary.income,
                expense = uiState.summary.expense,
                balance = uiState.summary.balance,
            )
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Text(
                    text = uiState.budgetWarningText,
                    modifier = Modifier.padding(12.dp),
                )
            }
        }
        item {
            AddTransactionCard(
                amount = uiState.amountInput,
                selectedType = uiState.selectedType,
                categories = uiState.categories,
                selectedCategoryId = uiState.selectedCategoryId,
                note = uiState.noteInput,
                date = uiState.dateInput,
                infoText = uiState.infoText,
                onAmountChange = viewModel::onAmountChange,
                onTypeChange = viewModel::onTypeChange,
                onCategorySelect = viewModel::onCategorySelect,
                onNoteChange = viewModel::onNoteChange,
                onDateChange = viewModel::onDateChange,
                onAddClick = viewModel::addTransaction,
            )
        }
        item {
            Text(
                text = "交易记录",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }

        if (uiState.groupedTransactions.isEmpty()) {
            item {
                Card {
                    Text(
                        text = "暂无记录，先添加一条吧。",
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        } else {
            items(uiState.groupedTransactions) { group ->
                TransactionDayGroupCard(group = group)
            }
        }
    }
}

@Composable
private fun SummaryCard(
    monthLabel: String,
    income: Double,
    expense: Double,
    balance: Double,
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = "$monthLabel 月度汇总", style = MaterialTheme.typography.titleMedium)
            SummaryRow(label = "收入", value = income.toMoneyText(), color = Color(0xFF2E7D32))
            SummaryRow(label = "支出", value = expense.toMoneyText(), color = Color(0xFFC62828))
            SummaryRow(label = "结余", value = balance.toMoneyText(), color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    color: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label)
        Text(text = value, color = color, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun AddTransactionCard(
    amount: String,
    selectedType: TransactionType,
    categories: List<com.jerry.magicledger.data.db.CategoryEntity>,
    selectedCategoryId: Long?,
    note: String,
    date: String,
    infoText: String?,
    onAmountChange: (String) -> Unit,
    onTypeChange: (TransactionType) -> Unit,
    onCategorySelect: (Long) -> Unit,
    onNoteChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onAddClick: () -> Unit,
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(text = "新增记账", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = amount,
                onValueChange = onAmountChange,
                label = { Text("金额") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedType == TransactionType.EXPENSE,
                    onClick = { onTypeChange(TransactionType.EXPENSE) },
                    label = { Text("支出") },
                )
                FilterChip(
                    selected = selectedType == TransactionType.INCOME,
                    onClick = { onTypeChange(TransactionType.INCOME) },
                    label = { Text("收入") },
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState()),
            ) {
                categories.forEach { category ->
                    FilterChip(
                        selected = selectedCategoryId == category.id,
                        onClick = { onCategorySelect(category.id) },
                        label = { Text(category.name) },
                    )
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = onNoteChange,
                label = { Text("备注") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = date,
                onValueChange = onDateChange,
                label = { Text("日期 (yyyy-MM-dd)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = onAddClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("保存记录")
            }

            if (!infoText.isNullOrBlank()) {
                Text(
                    text = infoText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun TransactionDayGroupCard(group: TransactionDayGroup) {
    Card {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(group.dateLabel, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "收 ${group.income.toMoneyText()}  支 ${group.expense.toMoneyText()}",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.End,
                )
            }
            group.items.forEachIndexed { index, item ->
                if (index > 0) {
                    Divider()
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        val noteText = item.note.ifBlank { "无备注" }
                        Text("${item.categoryName} · $noteText")
                        Text(
                            item.dateMillis.toDateTimeText(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    val isIncome = item.type == TransactionType.INCOME
                    val sign = if (isIncome) "+" else "-"
                    Text(
                        text = "$sign${item.amount.toMoneyText()}",
                        color = if (isIncome) Color(0xFF2E7D32) else Color(0xFFC62828),
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}
