package com.churchservants.popebooks.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = brown1,
    onPrimary = brown10,

    secondary = brown3,
    onSecondary = brown9,

    tertiary = brown5,
    onTertiary = brown8,

    background = brown10,
    onBackground = brown1,

    surface = brown10,
    onSurface = brown1,

    outlineVariant = brown9,
)

private val LightColorScheme = lightColorScheme(
    primary = brown10,
    onPrimary = brown1,

    secondary = brown8,
    onSecondary = brown2,

    tertiary = brown6,
    onTertiary = brown3,

    background = brown1,
    onBackground = brown10,

    surface = brown1,
    onSurface = brown10,

    outlineVariant = brown2,
)

@Composable
fun PopebooksTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}