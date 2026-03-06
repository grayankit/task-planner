package xyz.ankitgrai.kaizen.ui.theme

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import xyz.ankitgrai.kaizen.data.local.PreferencesFactory

enum class ThemeMode(val key: String, val displayName: String) {
    System("system", "System"),
    Light("light", "Light"),
    Dark("dark", "Dark");

    companion object {
        fun fromKey(key: String): ThemeMode {
            return entries.find { it.key == key } ?: System
        }
    }
}

class ThemeManager(private val prefs: PreferencesFactory) {

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_COLOR_THEME = "color_theme"
    }

    private val _themeMode = MutableStateFlow(
        ThemeMode.fromKey(prefs.getString(KEY_THEME_MODE, ThemeMode.System.key))
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _colorTheme = MutableStateFlow(
        ColorTheme.fromKey(prefs.getString(KEY_COLOR_THEME, ColorTheme.Blue.name))
    )
    val colorTheme: StateFlow<ColorTheme> = _colorTheme.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.putString(KEY_THEME_MODE, mode.key)
    }

    fun setColorTheme(theme: ColorTheme) {
        _colorTheme.value = theme
        prefs.putString(KEY_COLOR_THEME, theme.name)
    }
}
