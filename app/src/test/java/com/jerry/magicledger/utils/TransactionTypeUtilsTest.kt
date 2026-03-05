package com.jerry.magicledger.utils

import com.jerry.magicledger.data.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionTypeUtilsTest {
    @Test
    fun `validates transaction type correctly`() {
        assertTrue(TransactionType.INCOME.isValid())
        assertTrue(TransactionType.EXPENSE.isValid())
        assertFalse((null as TransactionType?).isValid())
    }

    @Test
    fun `returns correct sign for each type`() {
        assertEquals(1, TransactionType.INCOME.toSign())
        assertEquals(-1, TransactionType.EXPENSE.toSign())
    }
}
