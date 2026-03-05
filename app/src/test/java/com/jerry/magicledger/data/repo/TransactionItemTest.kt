package com.jerry.magicledger.data.repo

import com.jerry.magicledger.data.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionItemTest {
    @Test
    fun `sorting by date descending`() {
        val items = listOf(
            TransactionItem(id=1, amount=100.0, type=TransactionType.EXPENSE, categoryId=1L, categoryName="餐饮", note="lunch", dateMillis=1700000000000),
            TransactionItem(id=2, amount=50.0, type=TransactionType.INCOME, categoryId=1L, categoryName="工资", note="bonus", dateMillis=1701000000000),
            TransactionItem(id=3, amount=20.0, type=TransactionType.EXPENSE, categoryId=1L, categoryName="餐饮", note="coffee", dateMillis=1699900000000),
        )
        val sorted = items.sortedByDescending { it.dateMillis }
        assertEquals(2L, sorted[0].id)
        assertEquals(1L, sorted[1].id)
        assertEquals(3L, sorted[2].id)
    }
}
