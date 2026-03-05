package com.jerry.magicledger.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class DateUtilsTest {
    @Test
    fun `date formatting produces expected pattern`() {
        val millis = 1709251200000L // 2024-03-01
        val text = millis.toDateText()
        assertEquals("2024-03-01", text)
    }
}
