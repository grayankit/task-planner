package xyz.ankitgrai.taskplanner.util

import androidx.compose.runtime.Composable

actual fun isAndroid(): Boolean = false

@Composable
actual fun WidgetSettingsContent() {
    // No-op on desktop — widgets are Android-only
}
