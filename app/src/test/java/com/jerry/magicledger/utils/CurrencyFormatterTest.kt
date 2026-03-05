package com.jerry.magicledger.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyFormatterTest {
    @Test
    fun `formats positive amounts correctly`() {
        assertEquals("1,234.56", 1234.56.formatCurrency())
        assertEquals("0.00", 0.0.formatCurrency())
        assertEquals("100.00", 100.0.formatCurrency())
    }
}
