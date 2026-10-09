package com.example.breakevenlite.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.breakevenlite.data.model.AppThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = BrandTealLight,
    onPrimary = Slate900,
    primaryContainer = BrandTealContainerDark,
    onPrimaryContainer = BrandTealLight,
    secondary = Slate400,
    onSecondary = Slate900,
    secondaryContainer = Slate800,
    onSecondaryContainer = Slate200,
    tertiary = AmberWarningDark,
    onTertiary = Slate900,
    tertiaryContainer = AmberContainerDark,
    onTertiaryContainer = AmberContainerLight,
    background = Slate900,
    onBackground = Slate100,
    surface = Slate800,
    onSurface = Slate100,
    surfaceVariant = Slate700,
    onSurfaceVariant = Slate200,
    outline = Slate600,
    error = NegativeRedDark,
    onError = Slate900,
    errorContainer = NegativeContainerDark,
    onErrorContainer = NegativeContainerLight
)

private val LightColorScheme = lightColorScheme(
    primary = BrandTealDark,
    onPrimary = PureWhite,
    primaryContainer = BrandTealContainerLight,
    onPrimaryContainer = BrandTealDark,
    secondary = Slate700,
    onSecondary = PureWhite,
    secondaryContainer = Slate100,
    onSecondaryContainer = Slate900,
    tertiary = AmberWarning,
    onTertiary = PureWhite,
    tertiaryContainer = AmberContainerLight,
    onTertiaryContainer = AmberWarning,
    background = Slate50,
    onBackground = Slate900,
    surface = PureWhite,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate700,
    outline = Slate200,
    error = NegativeRed,
    onError = PureWhite,
    errorContainer = NegativeContainerLight,
    onErrorContainer = NegativeRed
)

@Composable
fun BreakEvenLiteTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
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