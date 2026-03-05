@file:OptIn(ExperimentalMaterial3Api::class)

package com.jerry.magicledger.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jerry.magicledger.data.TransactionType
import com.jerry.magicledger.data.db.CategoryEntity
import com.jerry.magicledger.data.repo.LedgerRepository
import com.jerry.magicledger.viewmodel.CategoryViewModel

@Composable
fun CategoryRoute(repository: LedgerRepository) {
    val viewModel: CategoryViewModel = viewModel(factory = CategoryViewModel.factory(repository))
    CategoryScreen(viewModel = viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryScreen(viewModel: CategoryViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val incomeCategories = uiState.categories.filter { it.type == TransactionType.INCOME }
    val expenseCategories = uiState.categories.filter { it.type == TransactionType.EXPENSE }

    LazyColumn(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            AddCategoryCard(
                name = uiState.nameInput,
                selectedType = uiState.selectedType,
                infoText = uiState.infoText,
                onNameChange = viewModel::onNameChange,
                onTypeChange = viewModel::onTypeChange,
                onAddClick = viewModel::addCategory,
            )
        }
        item {
            CategorySection(title = "收入分类", categories = incomeCategories)
        }
        item {
            CategorySection(title = "支出分类", categories = expenseCategories)
        }
    }
}

@Composable
private fun AddCategoryCard(
    name: String,
    selectedType: TransactionType,
    infoText: String?,
    onNameChange: (String) -> Unit,
    onTypeChange: (TransactionType) -> Unit,
    onAddClick: () -> Unit,
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(text = "分类管理", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("新分类名称") },
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
            Button(
                onClick = onAddClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("新增分类")
            }
            if (!infoText.isNullOrBlank()) {
                Text(text = infoText, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun CategorySection(
    title: String,
    categories: List<CategoryEntity>,
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (categories.isEmpty()) {
                Text("暂无分类", style = MaterialTheme.typography.bodyMedium)
            } else {
                categories.forEach { category ->
                    CategoryItem(category)
                }
            }
        }
    }
}

@Composable
private fun CategoryItem(category: CategoryEntity) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = category.name)
        AssistChip(
            onClick = { },
            label = { Text(if (category.isPreset) "预设" else "自定义") },
        )
    }
}
