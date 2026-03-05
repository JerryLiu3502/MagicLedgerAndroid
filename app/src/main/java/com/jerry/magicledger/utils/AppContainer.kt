package com.jerry.magicledger.utils

import android.content.Context

object AppContainer {
    @Volatile private var instance: AppContainer? = null
    private lateinit var appContext: Context
    
    fun init(context: Context) {
        appContext = context.applicationContext
    }
    
    val context: Context
        get() {
            check(::appContext.isInitialized) { "AppContainer not initialized" }
            return appContext
        }
    
    val preferences: PreferencesHelper by lazy { PreferencesHelper(context) }
    
    fun get(): AppContainer = instance ?: synchronized(this) {
        instance ?: AppContainer.also { instance = it }
    }
}
