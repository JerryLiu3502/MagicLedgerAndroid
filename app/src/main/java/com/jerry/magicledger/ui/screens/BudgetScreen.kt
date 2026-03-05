package com.jerry.magicledger.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jerry.magicledger.data.repo.LedgerRepository
import com.jerry.magicledger.ui.components.toMoneyText
import com.jerry.magicledger.viewmodel.BudgetViewModel

@Composable
fun BudgetRoute(repository: LedgerRepository) {
    val viewModel: BudgetViewModel = viewModel(factory = BudgetViewModel.factory(repository))
    BudgetScreen(viewModel = viewModel)
}

@Composable
private fun BudgetScreen(viewModel: BudgetViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(text = "${uiState.monthLabel} 预算看板", style = MaterialTheme.typography.titleMedium)
                    BudgetRow("收入", uiState.summary.income.toMoneyText(), Color(0xFF2E7D32))
                    BudgetRow("支出", uiState.summary.expense.toMoneyText(), Color(0xFFC62828))
                    BudgetRow("结余", uiState.summary.balance.toMoneyText(), MaterialTheme.colorScheme.primary)
                    BudgetRow(
                        "预算",
                        uiState.budgetAmount?.toMoneyText() ?: "未设置",
                        MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedTextField(
                        value = uiState.budgetInput,
                        onValueChange = viewModel::onBudgetInputChange,
                        label = { Text("本月预算金额") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        onClick = viewModel::saveBudget,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("保存预算")
                    }
                    if (!uiState.infoText.isNullOrBlank()) {
                        Text(text = uiState.infoText!!, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                Text(
                    text = uiState.warningText,
                    modifier = Modifier.padding(12.dp),
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun BudgetRow(
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
