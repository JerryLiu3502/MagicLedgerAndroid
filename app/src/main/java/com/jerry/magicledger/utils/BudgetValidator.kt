package com.jerry.magicledger.utils

fun isValidBudget(amount: Double?): Boolean {
    return amount != null && amount > 0.0 && amount < 1_000_000.0
}
