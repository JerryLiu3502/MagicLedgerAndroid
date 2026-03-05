package com.jerry.magicledger.utils

import com.jerry.magicledger.data.TransactionType

fun TransactionType?.isValid(): Boolean = this != null

fun TransactionType.toSign(): Int = when (this) {
    TransactionType.INCOME -> 1
    TransactionType.EXPENSE -> -1
}
