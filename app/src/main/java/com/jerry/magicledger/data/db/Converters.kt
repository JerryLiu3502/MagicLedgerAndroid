package com.jerry.magicledger.data.db

import androidx.room.TypeConverter
import com.jerry.magicledger.data.TransactionType

class Converters {
    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)

    @TypeConverter
    fun fromTransactionType(type: TransactionType): String = type.name
}
