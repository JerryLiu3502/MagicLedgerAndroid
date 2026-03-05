package com.jerry.magicledger.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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
}
