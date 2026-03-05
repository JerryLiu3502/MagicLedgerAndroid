package com.jerry.magicledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.jerry.magicledger.data.db.AppDatabase
import com.jerry.magicledger.data.repo.LedgerRepository
import com.jerry.magicledger.data.repo.LedgerRepositoryImpl
import com.jerry.magicledger.ui.MagicLedgerApp
import com.jerry.magicledger.ui.theme.MagicLedgerTheme

class MainActivity : ComponentActivity() {
    private val repository: LedgerRepository by lazy {
        LedgerRepositoryImpl(AppDatabase.getInstance(applicationContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MagicLedgerTheme {
                MagicLedgerApp(repository = repository)
            }
        }
    }
}
