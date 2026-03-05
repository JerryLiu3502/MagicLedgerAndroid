package com.jerry.magicledger.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BudgetValidatorTest {
    @Test
    fun `validates budget range correctly`() {
        assertTrue(isValidBudget(100.0))
        assertTrue(isValidBudget(999_999.99))
        assertFalse(isValidBudget(0.0))
        assertFalse(isValidBudget(-100.0))
        assertFalse(isValidBudget(1_000_000.0))
        assertFalse(isValidBudget(null))
    }
}
