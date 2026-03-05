package com.jerry.magicledger.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jerry.magicledger.data.repo.LedgerRepository
import com.jerry.magicledger.ui.components.toMoneyText
import com.jerry.magicledger.viewmodel.CategoryStatUiItem
import com.jerry.magicledger.viewmodel.StatsViewModel

@Composable
fun StatsRoute(repository: LedgerRepository) {
    val viewModel: StatsViewModel = viewModel(factory = StatsViewModel.factory(repository))
    StatsScreen(viewModel = viewModel)
}

@Composable
private fun StatsScreen(viewModel: StatsViewModel) {
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = viewModel::goPrevMonth) {
                            Text("← 上月")
                        }
                        Text("${uiState.monthLabel} 分类统计", style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = viewModel::goNextMonth) {
                            Text("下月 →")
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = viewModel::goCurrentMonth) {
                            Text("回到本月")
                        }
                    }
                    Text("收入 ${uiState.summary.income.toMoneyText()} · 支出 ${uiState.summary.expense.toMoneyText()}")
                }
            }
        }

        item {
            CategoryStatSection(
                title = "支出分类占比",
                items = uiState.expenseStats,
                color = Color(0xFFC62828),
                emptyText = "本月暂无支出记录",
            )
        }

        item {
            CategoryStatSection(
                title = "收入分类占比",
                items = uiState.incomeStats,
                color = Color(0xFF2E7D32),
                emptyText = "本月暂无收入记录",
            )
        }
    }
}

@Composable
private fun CategoryStatSection(
    title: String,
    items: List<CategoryStatUiItem>,
    color: Color,
    emptyText: String,
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (items.isEmpty()) {
                Text(
                    text = emptyText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                items.forEach { item ->
                    CategoryStatRow(
                        item = item,
                        color = color,
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryStatRow(
    item: CategoryStatUiItem,
    color: Color,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(item.name)
            Text(
                text = "${item.amount.toMoneyText()} · ${(item.ratio * 100).toInt()}%",
                fontWeight = FontWeight.Medium,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(item.ratio.coerceIn(0f, 1f))
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(color),
            )
        }
    }
}
