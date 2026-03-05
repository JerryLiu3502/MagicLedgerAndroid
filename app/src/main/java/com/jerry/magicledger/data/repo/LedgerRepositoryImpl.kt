package com.jerry.magicledger.data.repo

import com.jerry.magicledger.data.TransactionType
import com.jerry.magicledger.data.db.AppDatabase
import com.jerry.magicledger.data.db.BudgetEntity
import com.jerry.magicledger.data.db.CategoryEntity
import com.jerry.magicledger.data.db.TransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class LedgerRepositoryImpl(
    private val database: AppDatabase,
) : LedgerRepository {
    private val transactionDao = database.transactionDao()
    private val categoryDao = database.categoryDao()
    private val budgetDao = database.budgetDao()

    override fun observeTransactions(): Flow<List<TransactionItem>> {
        return transactionDao.observeTransactions().map { rows ->
            rows.map {
                TransactionItem(
                    id = it.id,
                    amount = it.amount,
                    type = it.type,
                    categoryId = it.categoryId,
                    categoryName = it.categoryName,
                    note = it.note,
                    dateMillis = it.dateMillis,
                )
            }
        }
    }

    override fun observeAllCategories(): Flow<List<CategoryEntity>> = categoryDao.observeAll()

    override fun observeCategoriesByType(type: TransactionType): Flow<List<CategoryEntity>> {
        return categoryDao.observeByType(type)
    }

    override fun observeMonthlySummary(month: YearMonth): Flow<MonthlySummary> {
        val (startMillis, endMillis) = month.toMillisRange()
        return transactionDao.observeMonthlySummary(startMillis, endMillis).map {
            MonthlySummary(income = it.income, expense = it.expense)
        }
    }

    override fun observeBudget(month: YearMonth): Flow<Double?> {
        return budgetDao.observeByMonth(month.toMonthKey()).map { it?.budgetAmount }
    }

    override suspend fun addTransaction(
        amount: Double,
        type: TransactionType,
        categoryId: Long,
        note: String,
        dateMillis: Long,
    ) {
        transactionDao.insert(
            TransactionEntity(
                amount = amount,
                type = type,
                categoryId = categoryId,
                note = note,
                dateMillis = dateMillis,
            ),
        )
    }

    override suspend fun addCategory(name: String, type: TransactionType): Boolean {
        val normalizedName = name.trim()
        if (normalizedName.isBlank()) {
            return false
        }
        if (categoryDao.findByNameAndType(normalizedName, type) != null) {
            return false
        }
        categoryDao.insert(
            CategoryEntity(
                name = normalizedName,
                type = type,
                isPreset = false,
            ),
        )
        return true
    }

    override suspend fun updateTransaction(
        transactionId: Long,
        amount: Double,
        type: TransactionType,
        categoryId: Long,
        note: String,
        dateMillis: Long,
    ): Boolean {
        val updatedRows = transactionDao.updateById(
            id = transactionId,
            amount = amount,
            type = type,
            categoryId = categoryId,
            note = note,
            dateMillis = dateMillis,
        )
        return updatedRows > 0
    }

    override suspend fun deleteTransaction(transactionId: Long) {
        transactionDao.deleteById(transactionId)
    }

    override suspend fun setBudget(month: YearMonth, budgetAmount: Double) {
        budgetDao.upsert(
            BudgetEntity(
                monthKey = month.toMonthKey(),
                budgetAmount = budgetAmount,
            ),
        )
    }

    override suspend fun seedIfNeeded() {
        if (categoryDao.count() > 0) {
            return
        }

        val presetCategories = listOf(
            CategoryEntity(name = "工资", type = TransactionType.INCOME, isPreset = true),
            CategoryEntity(name = "奖金", type = TransactionType.INCOME, isPreset = true),
            CategoryEntity(name = "餐饮", type = TransactionType.EXPENSE, isPreset = true),
            CategoryEntity(name = "交通", type = TransactionType.EXPENSE, isPreset = true),
            CategoryEntity(name = "购物", type = TransactionType.EXPENSE, isPreset = true),
            CategoryEntity(name = "娱乐", type = TransactionType.EXPENSE, isPreset = true),
            CategoryEntity(name = "住房", type = TransactionType.EXPENSE, isPreset = true),
            CategoryEntity(name = "健康", type = TransactionType.EXPENSE, isPreset = true),
        )
        val ids = categoryDao.insertAll(presetCategories)
        val categoryIdByName = presetCategories.mapIndexed { index, category ->
            category.name to ids[index]
        }.toMap()

        val now = LocalDate.now()
        val zone = ZoneId.systemDefault()
        fun dayMillis(daysAgo: Long): Long {
            return now.minusDays(daysAgo).atStartOfDay(zone).toInstant().toEpochMilli()
        }

        val sampleTransactions = listOf(
            TransactionEntity(
                amount = 12500.0,
                type = TransactionType.INCOME,
                categoryId = categoryIdByName.getValue("工资"),
                note = "3月工资到账",
                dateMillis = dayMillis(4),
            ),
            TransactionEntity(
                amount = 68.0,
                type = TransactionType.EXPENSE,
                categoryId = categoryIdByName.getValue("餐饮"),
                note = "午餐",
                dateMillis = dayMillis(3),
            ),
            TransactionEntity(
                amount = 22.0,
                type = TransactionType.EXPENSE,
                categoryId = categoryIdByName.getValue("交通"),
                note = "地铁",
                dateMillis = dayMillis(2),
            ),
            TransactionEntity(
                amount = 229.0,
                type = TransactionType.EXPENSE,
                categoryId = categoryIdByName.getValue("购物"),
                note = "生活用品",
                dateMillis = dayMillis(1),
            ),
            TransactionEntity(
                amount = 120.0,
                type = TransactionType.EXPENSE,
                categoryId = categoryIdByName.getValue("娱乐"),
                note = "电影和咖啡",
                dateMillis = dayMillis(0),
            ),
            TransactionEntity(
                amount = 300.0,
                type = TransactionType.INCOME,
                categoryId = categoryIdByName.getValue("奖金"),
                note = "项目奖励",
                dateMillis = dayMillis(0),
            ),
        )

        transactionDao.insertAll(sampleTransactions)
        setBudget(YearMonth.now(), 5000.0)
    }
}

private fun YearMonth.toMonthKey(): String = "${year}-${monthValue.toString().padStart(2, '0')}"

private fun YearMonth.toMillisRange(): Pair<Long, Long> {
    val zone = ZoneId.systemDefault()
    val start = atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
    val end = plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
    return start to end
}
