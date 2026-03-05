package com.jerry.magicledger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jerry.magicledger.data.repo.LedgerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DataUiState(
    val exportJson: String = "",
    val importJsonInput: String = "",
    val infoText: String? = null,
)

class DataViewModel(
    private val repository: LedgerRepository,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(DataUiState())
    val uiState: StateFlow<DataUiState> = mutableUiState.asStateFlow()

    fun onImportJsonInputChange(value: String) {
        mutableUiState.value = mutableUiState.value.copy(importJsonInput = value)
    }

    fun exportData() {
        viewModelScope.launch {
            val json = repository.exportDataAsJson()
            mutableUiState.value = mutableUiState.value.copy(
                exportJson = json,
                infoText = "导出成功，可复制保存",
            )
        }
    }

    fun importData() {
        val rawJson = uiState.value.importJsonInput
        if (rawJson.isBlank()) {
            mutableUiState.value = mutableUiState.value.copy(infoText = "先粘贴要导入的 JSON")
            return
        }

        viewModelScope.launch {
            runCatching {
                repository.importDataFromJson(rawJson)
            }.onSuccess { result ->
                mutableUiState.value = mutableUiState.value.copy(
                    infoText = "导入完成：分类 ${result.categoryCount}，记录 ${result.transactionCount}，预算 ${result.budgetCount}",
                )
            }.onFailure {
                mutableUiState.value = mutableUiState.value.copy(
                    infoText = "导入失败：JSON 格式不合法或字段缺失",
                )
            }
        }
    }

    companion object {
        fun factory(repository: LedgerRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DataViewModel(repository) as T
                }
            }
        }
    }
}
