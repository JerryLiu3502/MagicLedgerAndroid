package com.jerry.magicledger.utils

const val MIN_CATEGORY_NAME_LENGTH = 1
const val MAX_CATEGORY_NAME_LENGTH = 20

fun String?.isValidCategoryName(): Boolean {
    val trimmed = this?.trim()
    return trimmed != null && trimmed.length in MIN_CATEGORY_NAME_LENGTH..MAX_CATEGORY_NAME_LENGTH
}

fun String?.normalizeCategoryName(): String? {
    return this?.trim()?.take(MAX_CATEGORY_NAME_LENGTH)?.takeIf { it.isNotEmpty() }
}
