package xyz.ankitgrai.taskplanner.util

import androidx.compose.runtime.Composable

/**
 * Detect whether we're running on Android at runtime.
 * Used to conditionally show Android-only features like "Check for Updates".
 */
expect fun isAndroid(): Boolean

/**
 * Android-only widget settings section for the Settings screen.
 * On Android, displays a list of active home screen widgets with
 * their current configuration and allows reconfiguration.
 * On desktop, this is a no-op.
 */
@Composable
expect fun WidgetSettingsContent()
