package com.jerry.magicledger.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jerry.magicledger.data.repo.LedgerRepository
import com.jerry.magicledger.viewmodel.DataViewModel

@Composable
fun DataRoute(repository: LedgerRepository) {
    val viewModel: DataViewModel = viewModel(factory = DataViewModel.factory(repository))
    DataScreen(viewModel)
}

@Composable
private fun DataScreen(viewModel: DataViewModel) {
    val clipboard = LocalClipboardManager.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("数据导出", style = MaterialTheme.typography.titleMedium)
                    Button(
                        onClick = viewModel::exportData,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("生成导出 JSON")
                    }
                    if (uiState.exportJson.isNotBlank()) {
                        TextButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(uiState.exportJson))
                            },
                        ) {
                            Text("复制导出内容")
                        }
                    }
                    OutlinedTextField(
                        value = uiState.exportJson,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("导出内容（只读）") },
                        minLines = 6,
                        maxLines = 12,
                        readOnly = true,
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
                    Text("数据导入", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = uiState.importJsonInput,
                        onValueChange = viewModel::onImportJsonInputChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("粘贴 JSON") },
                        minLines = 6,
                        maxLines = 12,
                    )
                    Button(
                        onClick = viewModel::importData,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("导入并覆盖本地数据")
                    }
                }
            }
        }

        if (!uiState.infoText.isNullOrBlank()) {
            item {
                Text(
                    text = uiState.infoText ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
            }
        }
    }
}
