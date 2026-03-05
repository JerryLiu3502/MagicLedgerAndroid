package com.jerry.magicledger.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AmountParserTest {
    @Test
    fun `returns null for blank or invalid input`() {
        assertNull(parseAmountOrNull(""))
        assertNull(parseAmountOrNull("  "))
        assertNull(parseAmountOrNull("abc"))
    }

    @Test
    fun `returns null for zero or negative values`() {
        assertNull(parseAmountOrNull("0"))
        assertNull(parseAmountOrNull("-1"))
    }

    @Test
    fun `parses dot or comma decimal`() {
        assertEquals(12.5, parseAmountOrNull("12.5")!!, 0.0001)
        assertEquals(12.5, parseAmountOrNull("12,5")!!, 0.0001)
    }
}
