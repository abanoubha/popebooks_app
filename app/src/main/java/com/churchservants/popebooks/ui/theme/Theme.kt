package com.churchservants.popebooks.ui.theme

import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

private val DarkColorScheme = darkColorScheme(
    primary = brown1,
    onPrimary = brown10,

    secondary = brown3,
    onSecondary = brown9,

    tertiary = brown5,
    onTertiary = brown8,

    background = brown10,
    onBackground = brown1,

    surface = brown9,
    onSurface = brown1,

    surfaceVariant = brown8,
    onSurfaceVariant = brown1,

    outlineVariant = brown7,
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

    surface = brown2,
    onSurface = brown10,

    surfaceVariant = brown3,
    onSurfaceVariant = brown10,

    outlineVariant = brown4,
)

@Composable
fun PopebooksTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    fontScale: Float? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE) }
    val actualFontScale = fontScale ?: sharedPreferences.getFloat("font_scale", 1.0f)

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val currentDensity = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(
            density = currentDensity.density,
            fontScale = currentDensity.fontScale * actualFontScale
        )
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}