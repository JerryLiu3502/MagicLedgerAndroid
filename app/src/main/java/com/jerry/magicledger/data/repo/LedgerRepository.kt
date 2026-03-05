package com.jerry.magicledger.data.repo

import com.jerry.magicledger.data.TransactionType
import com.jerry.magicledger.data.db.CategoryEntity
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

data class MonthlySummary(
    val income: Double,
    val expense: Double,
) {
    val balance: Double
        get() = income - expense
}

data class TransactionItem(
    val id: Long,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long,
    val categoryName: String,
    val note: String,
    val dateMillis: Long,
)

data class CategorySummaryItem(
    val categoryName: String,
    val type: TransactionType,
    val totalAmount: Double,
)

data class ImportResult(
    val categoryCount: Int,
    val transactionCount: Int,
    val budgetCount: Int,
)

interface LedgerRepository {
    fun observeTransactions(): Flow<List<TransactionItem>>
    fun observeAllCategories(): Flow<List<CategoryEntity>>
    fun observeCategoriesByType(type: TransactionType): Flow<List<CategoryEntity>>
    fun observeMonthlySummary(month: YearMonth): Flow<MonthlySummary>
    fun observeMonthlyCategorySummary(month: YearMonth): Flow<List<CategorySummaryItem>>
    fun observeBudget(month: YearMonth): Flow<Double?>

    suspend fun addTransaction(
        amount: Double,
        type: TransactionType,
        categoryId: Long,
        note: String,
        dateMillis: Long,
    )

    suspend fun addCategory(name: String, type: TransactionType): Boolean
    suspend fun updateTransaction(
        transactionId: Long,
        amount: Double,
        type: TransactionType,
        categoryId: Long,
        note: String,
        dateMillis: Long,
    ): Boolean

    suspend fun deleteTransaction(transactionId: Long)
    suspend fun setBudget(month: YearMonth, budgetAmount: Double)
    suspend fun exportDataAsJson(): String
    suspend fun importDataFromJson(rawJson: String): ImportResult
    suspend fun seedIfNeeded()
}
