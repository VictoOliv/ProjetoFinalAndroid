package com.estudaai.userinterface.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CoresClaras = lightColorScheme(
    primary = Color(0xFF1B7F4C),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB9F0CF),
    onPrimaryContainer = Color(0xFF00210F),
    secondary = Color(0xFF4E6355),
    secondaryContainer = Color(0xFFD0E8D6),
    background = Color(0xFFF6FBF5),
    surface = Color(0xFFF6FBF5)
)

private val CoresEscuras = darkColorScheme(
    primary = Color(0xFF8DD8AE),
    onPrimary = Color(0xFF00391D),
    primaryContainer = Color(0xFF00522C),
    onPrimaryContainer = Color(0xFFB9F0CF),
    secondary = Color(0xFFB4CCBA),
    secondaryContainer = Color(0xFF364B3E)
)

@Composable
fun EstudaAiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) CoresEscuras else CoresClaras,
        content = content
    )
}
