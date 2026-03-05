package com.jerry.magicledger.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jerry.magicledger.data.TransactionType
import com.jerry.magicledger.data.db.CategoryEntity
import com.jerry.magicledger.data.repo.LedgerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoryUiState(
    val nameInput: String = "",
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val categories: List<CategoryEntity> = emptyList(),
    val infoText: String? = null,
)

class CategoryViewModel(
    private val repository: LedgerRepository,
) : ViewModel() {
    private val nameInput = MutableStateFlow("")
    private val selectedType = MutableStateFlow(TransactionType.EXPENSE)
    private val infoText = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CategoryUiState> = combine(
        nameInput,
        selectedType,
        repository.observeAllCategories(),
        infoText,
    ) { name, type, categories, info ->
        CategoryUiState(
            nameInput = name,
            selectedType = type,
            categories = categories,
            infoText = info,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CategoryUiState())

    init {
        viewModelScope.launch {
            repository.seedIfNeeded()
        }
    }

    fun onNameChange(value: String) {
        nameInput.value = value
    }

    fun onTypeChange(type: TransactionType) {
        selectedType.value = type
    }

    fun dismissInfo() {
        infoText.value = null
    }

    fun addCategory() {
        viewModelScope.launch {
            val name = nameInput.value.trim()
            if (name.isBlank()) {
                infoText.value = "请输入分类名"
                return@launch
            }
            val success = repository.addCategory(name, selectedType.value)
            if (success) {
                nameInput.value = ""
                infoText.value = "已新增分类"
            } else {
                infoText.value = "该分类已存在"
            }
        }
    }

    companion object {
        fun factory(repository: LedgerRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CategoryViewModel(repository) as T
                }
            }
        }
    }
}
