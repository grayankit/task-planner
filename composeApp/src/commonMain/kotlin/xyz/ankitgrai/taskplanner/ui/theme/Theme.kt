package xyz.ankitgrai.taskplanner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.koinInject

@Composable
fun TaskPlannerTheme(
    content: @Composable () -> Unit,
) {
    val themeManager = koinInject<ThemeManager>()
    val themeMode by themeManager.themeMode.collectAsState()
    val colorTheme by themeManager.colorTheme.collectAsState()

    val isDark = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = colorTheme.darkPrimary,
            onPrimary = colorTheme.darkOnPrimary,
            primaryContainer = colorTheme.darkPrimaryContainer,
            onPrimaryContainer = colorTheme.darkOnPrimaryContainer,
            secondary = colorTheme.darkSecondary,
            onSecondary = colorTheme.darkOnSecondary,
            secondaryContainer = colorTheme.darkSecondaryContainer,
            onSecondaryContainer = colorTheme.darkOnSecondaryContainer,
            tertiary = colorTheme.darkTertiary,
            onTertiary = colorTheme.darkOnTertiary,
            background = DarkBackground,
            onBackground = DarkOnBackground,
            surface = DarkSurface,
            onSurface = DarkOnSurface,
            error = Error,
            onError = OnError,
        )
    } else {
        lightColorScheme(
            primary = colorTheme.lightPrimary,
            onPrimary = colorTheme.lightOnPrimary,
            primaryContainer = colorTheme.lightPrimaryContainer,
            onPrimaryContainer = colorTheme.lightOnPrimaryContainer,
            secondary = colorTheme.lightSecondary,
            onSecondary = colorTheme.lightOnSecondary,
            secondaryContainer = colorTheme.lightSecondaryContainer,
            onSecondaryContainer = colorTheme.lightOnSecondaryContainer,
            tertiary = colorTheme.lightTertiary,
            onTertiary = colorTheme.lightOnTertiary,
            background = Background,
            onBackground = OnBackground,
            surface = Surface,
            onSurface = OnSurface,
            error = Error,
            onError = OnError,
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content,
    )
}
