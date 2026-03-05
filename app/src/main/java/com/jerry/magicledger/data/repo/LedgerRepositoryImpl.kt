package com.jerry.magicledger.data.repo

import com.jerry.magicledger.data.TransactionType
import com.jerry.magicledger.data.db.AppDatabase
import com.jerry.magicledger.data.db.BudgetEntity
import com.jerry.magicledger.data.db.CategoryEntity
import androidx.room.withTransaction
import com.jerry.magicledger.data.db.TransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
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

    override fun observeMonthlyCategorySummary(month: YearMonth): Flow<List<CategorySummaryItem>> {
        val (startMillis, endMillis) = month.toMillisRange()
        return transactionDao.observeCategorySummaryByMonth(startMillis, endMillis).map { rows ->
            rows.map {
                CategorySummaryItem(
                    categoryName = it.categoryName,
                    type = it.type,
                    totalAmount = it.totalAmount,
                )
            }
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

    override suspend fun exportDataAsJson(): String {
        val categories = categoryDao.getAllEntities()
        val categoryById = categories.associateBy { it.id }
        val budgets = budgetDao.getAllEntities()
        val transactions = transactionDao.getAllEntities()

        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val categoryArray = JSONArray().apply {
            categories.forEach {
                put(
                    JSONObject()
                        .put("name", it.name)
                        .put("type", it.type.name)
                        .put("isPreset", it.isPreset),
                )
            }
        }

        val budgetArray = JSONArray().apply {
            budgets.forEach {
                put(
                    JSONObject()
                        .put("monthKey", it.monthKey)
                        .put("budgetAmount", it.budgetAmount),
                )
            }
        }

        val transactionArray = JSONArray().apply {
            transactions.forEach {
                val category = categoryById[it.categoryId]
                put(
                    JSONObject()
                        .put("amount", it.amount)
                        .put("type", it.type.name)
                        .put("categoryName", category?.name ?: "Unknown")
                        .put("note", it.note)
                        .put("dateMillis", it.dateMillis),
                )
            }
        }

        root.put("categories", categoryArray)
        root.put("budgets", budgetArray)
        root.put("transactions", transactionArray)

        return root.toString(2)
    }

    override suspend fun importDataFromJson(rawJson: String): ImportResult {
        val root = JSONObject(rawJson)
        val categoriesRaw = root.optJSONArray("categories") ?: JSONArray()
        val budgetsRaw = root.optJSONArray("budgets") ?: JSONArray()
        val transactionsRaw = root.optJSONArray("transactions") ?: JSONArray()

        val importedCategories = mutableListOf<CategoryEntity>()
        for (index in 0 until categoriesRaw.length()) {
            val item = categoriesRaw.optJSONObject(index) ?: continue
            val name = item.optString("name").trim()
            val typeName = item.optString("type").uppercase()
            if (name.isBlank()) {
                continue
            }
            val type = runCatching { TransactionType.valueOf(typeName) }.getOrNull() ?: continue
            importedCategories += CategoryEntity(
                name = name,
                type = type,
                isPreset = item.optBoolean("isPreset", false),
            )
        }

        val importedBudgets = mutableListOf<BudgetEntity>()
        for (index in 0 until budgetsRaw.length()) {
            val item = budgetsRaw.optJSONObject(index) ?: continue
            val monthKey = item.optString("monthKey").trim()
            val budgetAmount = item.optDouble("budgetAmount", Double.NaN)
            if (monthKey.isBlank() || budgetAmount.isNaN()) {
                continue
            }
            importedBudgets += BudgetEntity(
                monthKey = monthKey,
                budgetAmount = budgetAmount,
            )
        }

        data class ImportedTx(
            val amount: Double,
            val type: TransactionType,
            val categoryName: String,
            val note: String,
            val dateMillis: Long,
        )

        val importedTransactions = mutableListOf<ImportedTx>()
        for (index in 0 until transactionsRaw.length()) {
            val item = transactionsRaw.optJSONObject(index) ?: continue
            val amount = item.optDouble("amount", Double.NaN)
            val typeName = item.optString("type").uppercase()
            val categoryName = item.optString("categoryName").trim()
            val note = item.optString("note")
            val dateMillis = item.optLong("dateMillis", -1L)
            if (amount.isNaN() || categoryName.isBlank() || dateMillis <= 0L) {
                continue
            }
            val type = runCatching { TransactionType.valueOf(typeName) }.getOrNull() ?: continue
            importedTransactions += ImportedTx(
                amount = amount,
                type = type,
                categoryName = categoryName,
                note = note,
                dateMillis = dateMillis,
            )
        }

        var writtenCategoryCount = 0
        var writtenBudgetCount = 0
        var writtenTransactionCount = 0

        database.withTransaction {
            transactionDao.deleteAll()
            budgetDao.deleteAll()
            categoryDao.deleteAll()

            val categoryMap = linkedMapOf<Pair<String, TransactionType>, Long>()

            val dedupedCategories = importedCategories
                .distinctBy { it.name.lowercase() to it.type }
                .sortedBy { it.name }

            dedupedCategories.forEach { category ->
                val id = categoryDao.insert(category)
                categoryMap[category.name.lowercase() to category.type] = id
            }
            writtenCategoryCount = dedupedCategories.size

            importedBudgets.forEach { budgetDao.upsert(it) }
            writtenBudgetCount = importedBudgets.size

            val txEntities = importedTransactions.mapNotNull { tx ->
                val categoryId = categoryMap[tx.categoryName.lowercase() to tx.type]
                    ?: run {
                        val fallbackCategoryId = categoryMap["其他" to tx.type]
                        if (fallbackCategoryId != null) {
                            fallbackCategoryId
                        } else {
                            val newId = categoryDao.insert(
                                CategoryEntity(
                                    name = "其他",
                                    type = tx.type,
                                    isPreset = false,
                                ),
                            )
                            categoryMap["其他" to tx.type] = newId
                            writtenCategoryCount += 1
                            newId
                        }
                    }
                TransactionEntity(
                    amount = tx.amount,
                    type = tx.type,
                    categoryId = categoryId,
                    note = tx.note,
                    dateMillis = tx.dateMillis,
                )
            }

            transactionDao.insertAll(txEntities)
            writtenTransactionCount = txEntities.size
        }

        return ImportResult(
            categoryCount = writtenCategoryCount,
            transactionCount = writtenTransactionCount,
            budgetCount = writtenBudgetCount,
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
