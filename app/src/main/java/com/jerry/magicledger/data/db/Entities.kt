package com.jerry.magicledger.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jerry.magicledger.data.TransactionType

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long,
    val note: String,
    val dateMillis: Long,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: TransactionType,
    val isPreset: Boolean,
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val monthKey: String,
    val budgetAmount: Double,
)

data class TransactionRecord(
    val id: Long,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long,
    val categoryName: String,
    val note: String,
    val dateMillis: Long,
)

data class MonthlySummaryRow(
    @ColumnInfo(name = "income") val income: Double,
    @ColumnInfo(name = "expense") val expense: Double,
)
