package com.jerry.magicledger.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesHelper(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("magic_ledger_prefs", Context.MODE_PRIVATE)
    
    fun getBoolean(key: String, default: Boolean = false): Boolean = prefs.getBoolean(key, default)
    fun getInt(key: String, default: Int = 0): Int = prefs.getInt(key, default)
    fun getFloat(key: String, default: Float = 0f): Float = prefs.getFloat(key, default)
    fun getString(key: String, default: String? = null): String? = prefs.getString(key, default)
    fun getLong(key: String, default: Long = 0L): Long = prefs.getLong(key, default)
    
    fun setBoolean(key: String, value: Boolean) = prefs.edit().putBoolean(key, value).apply()
    fun setInt(key: String, value: Int) = prefs.edit().putInt(key, value).apply()
    fun setFloat(key: String, value: Float) = prefs.edit().putFloat(key, value).apply()
    fun setString(key: String, value: String?) = prefs.edit().putString(key, value).apply()
    fun setLong(key: String, value: Long) = prefs.edit().putLong(key, value).apply()
    
    fun remove(key: String) = prefs.edit().remove(key).apply()
    fun clear() = prefs.edit().clear().apply()
}
