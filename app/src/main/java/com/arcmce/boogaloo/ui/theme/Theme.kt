package com.arcmce.boogaloo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary                = Neutral90,
    onPrimary              = Neutral10,
    primaryContainer       = Neutral30,
    onPrimaryContainer     = Neutral90,
    secondary              = Neutral80,
    onSecondary            = Neutral20,
    secondaryContainer     = Neutral30,
    onSecondaryContainer   = Neutral90,
    tertiary               = Neutral60,
    onTertiary             = Neutral10,
    background             = Neutral10,
    onBackground           = Neutral90,
    surface                = Neutral20,
    onSurface              = Neutral90,
    surfaceVariant         = Neutral30,
    onSurfaceVariant       = Neutral80,
    outline                = Neutral40,
)

private val LightColorScheme = lightColorScheme(
    primary                = Neutral10,
    onPrimary              = Color.White,
    primaryContainer       = Neutral90,
    onPrimaryContainer     = Neutral10,
    secondary              = Neutral40,
    onSecondary            = Color.White,
    secondaryContainer     = Neutral90,
    onSecondaryContainer   = Neutral10,
    tertiary               = Neutral60,
    onTertiary             = Color.White,
    background             = Color.White,
    onBackground           = Neutral10,
    surface                = Neutral99,
    onSurface              = Neutral10,
    surfaceVariant         = Neutral95,
    onSurfaceVariant       = Neutral40,
    outline                = Neutral80,
)

@Composable
fun BoogalooJetpackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
