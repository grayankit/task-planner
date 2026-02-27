package xyz.ankitgrai.taskplanner.util

/**
 * Detect whether we're running on Android at runtime.
 * Used to conditionally show Android-only features like "Check for Updates".
 */
expect fun isAndroid(): Boolean
