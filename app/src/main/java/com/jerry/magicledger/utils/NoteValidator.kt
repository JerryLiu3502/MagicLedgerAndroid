package com.jerry.magicledger.utils

const val MAX_NOTE_LENGTH = 100

fun String?.isValidNote(): Boolean {
    return this != null && this.trim().length <= MAX_NOTE_LENGTH
}

fun String?.truncateNote(): String? {
    return this?.trim()?.take(MAX_NOTE_LENGTH)?.takeIf { it.isNotEmpty() }
}
