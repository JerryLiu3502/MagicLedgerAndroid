package com.jerry.magicledger.utils

fun String?.isNullOrBlankTrimmed(): Boolean {
    return this?.trim().isNullOrBlank()
}

fun String?.orEmptyTrimmed(): String {
    return this?.trim().orEmpty()
}
