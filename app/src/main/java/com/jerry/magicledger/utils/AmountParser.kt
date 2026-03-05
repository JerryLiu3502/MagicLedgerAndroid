package com.jerry.magicledger.utils

fun parseAmountOrNull(input: String): Double? {
    val normalized = input.trim().replace(',', '.')
    if (normalized.isBlank()) return null
    val value = normalized.toDoubleOrNull() ?: return null
    return if (value > 0.0) value else null
}
