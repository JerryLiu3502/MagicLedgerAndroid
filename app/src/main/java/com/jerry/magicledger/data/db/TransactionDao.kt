package com.jerry.magicledger.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jerry.magicledger.data.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query(
        """
        UPDATE transactions
        SET amount = :amount,
            type = :type,
            categoryId = :categoryId,
            note = :note,
            dateMillis = :dateMillis
        WHERE id = :id
        """,
    )
    suspend fun updateById(
        id: Long,
        amount: Double,
        type: TransactionType,
        categoryId: Long,
        note: String,
        dateMillis: Long,
    ): Int

    @Query(
        """
        SELECT
            t.id,
            t.amount,
            t.type,
            t.categoryId,
            COALESCE(c.name, 'Unknown') AS categoryName,
            t.note,
            t.dateMillis
        FROM transactions t
        LEFT JOIN categories c ON t.categoryId = c.id
        ORDER BY t.dateMillis DESC, t.createdAt DESC
        """,
    )
    fun observeTransactions(): Flow<List<TransactionRecord>>

    @Query(
        """
        SELECT
            COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amount END), 0) AS income,
            COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amount END), 0) AS expense
        FROM transactions
        WHERE dateMillis >= :startMillis AND dateMillis < :endMillis
        """,
    )
    fun observeMonthlySummary(startMillis: Long, endMillis: Long): Flow<MonthlySummaryRow>

    @Query(
        """
        SELECT
            COALESCE(c.name, 'Unknown') AS categoryName,
            t.type AS type,
            COALESCE(SUM(t.amount), 0) AS totalAmount
        FROM transactions t
        LEFT JOIN categories c ON t.categoryId = c.id
        WHERE t.dateMillis >= :startMillis AND t.dateMillis < :endMillis
        GROUP BY t.type, t.categoryId, c.name
        ORDER BY t.type ASC, totalAmount DESC
        """,
    )
    fun observeCategorySummaryByMonth(startMillis: Long, endMillis: Long): Flow<List<CategorySummaryRow>>
}
