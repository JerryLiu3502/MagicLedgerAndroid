package com.jerry.magicledger.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Upsert
    suspend fun upsert(entity: BudgetEntity)

    @Query("SELECT * FROM budgets WHERE monthKey = :monthKey LIMIT 1")
    fun observeByMonth(monthKey: String): Flow<BudgetEntity?>

    @Query("SELECT * FROM budgets ORDER BY monthKey ASC")
    suspend fun getAllEntities(): List<BudgetEntity>

    @Query("DELETE FROM budgets")
    suspend fun deleteAll()
}
