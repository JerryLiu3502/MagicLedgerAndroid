package com.jerry.magicledger.utils

import android.content.Context

val Context.preferences: PreferencesHelper
    get() = PreferencesHelper(this)
