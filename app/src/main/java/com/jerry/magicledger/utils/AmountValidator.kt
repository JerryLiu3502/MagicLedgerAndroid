package com.jerry.magicledger.utils

const val MIN_AMOUNT = 0.01
const val MAX_AMOUNT = 1_000_000.0

fun Double?.isValidAmount(): Boolean {
    return this != null && this in MIN_AMOUNT..MAX_AMOUNT
}

fun Double?.clampAmount(): Double? {
    return this?.coerceIn(MIN_AMOUNT, MAX_AMOUNT)?.takeIf { it > 0 }
}
