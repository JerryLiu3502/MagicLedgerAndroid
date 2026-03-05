package com.jerry.magicledger.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.jerry.magicledger.data.repo.LedgerRepository
import com.jerry.magicledger.ui.screens.BudgetRoute
import com.jerry.magicledger.ui.screens.CategoryRoute
import com.jerry.magicledger.ui.screens.DataRoute
import com.jerry.magicledger.ui.screens.HomeRoute
import com.jerry.magicledger.ui.screens.StatsRoute

private enum class AppTab(
    val title: String,
    val iconText: String,
) {
    Ledger("账本", "📒"),
    Categories("分类", "🏷️"),
    Budget("预算", "💰"),
    Stats("统计", "📊"),
    Data("数据", "💾"),
}

@Composable
fun MagicLedgerApp(repository: LedgerRepository) {
    var selectedTab by rememberSaveable { mutableStateOf(AppTab.Ledger) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                AppTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Text(tab.iconText) },
                        label = { Text(tab.title) },
                    )
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (selectedTab) {
                AppTab.Ledger -> HomeRoute(repository = repository)
                AppTab.Categories -> CategoryRoute(repository = repository)
                AppTab.Budget -> BudgetRoute(repository = repository)
                AppTab.Stats -> StatsRoute(repository = repository)
                AppTab.Data -> DataRoute(repository = repository)
            }
        }
    }
}
