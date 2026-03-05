package com.jerry.magicledger.ui.components

import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale.CHINA)
private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

fun Double.toMoneyText(): String = currencyFormatter.format(this)

fun Long.toDateTimeText(): String {
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .format(dateTimeFormatter)
}
