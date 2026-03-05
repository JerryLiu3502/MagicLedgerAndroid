package com.jerry.magicledger.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = LedgerBlue,
    secondary = LedgerGreen,
    error = LedgerRed,
    background = LedgerBackground,
)

private val DarkColors = darkColorScheme(
    primary = LedgerBlue,
    secondary = LedgerGreen,
    error = LedgerRed,
)

@Composable
fun MagicLedgerTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content,
    )
}
