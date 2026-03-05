package com.jerry.magicledger.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jerry.magicledger.data.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>): List<Long>

    @Query("SELECT * FROM categories ORDER BY isPreset DESC, name ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY isPreset DESC, name ASC")
    fun observeByType(type: TransactionType): Flow<List<CategoryEntity>>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int

    @Query("SELECT * FROM categories WHERE LOWER(name) = LOWER(:name) AND type = :type LIMIT 1")
    suspend fun findByNameAndType(name: String, type: TransactionType): CategoryEntity?

    @Query("SELECT * FROM categories ORDER BY id ASC")
    suspend fun getAllEntities(): List<CategoryEntity>

    @Query("DELETE FROM categories")
    suspend fun deleteAll()
}
