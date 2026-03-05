package com.jerry.magicledger.utils

import java.text.DecimalFormat

private val currencyFormat = DecimalFormat("#,##0.00")

fun Double.formatCurrency(): String {
    return currencyFormat.format(this)
}
