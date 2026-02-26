package com.taskplanner.ui.theme

import androidx.compose.ui.graphics.Color

// Priority colors
val PriorityCritical = Color(0xFFE53935)
val PriorityHigh = Color(0xFFFB8C00)
val PriorityMedium = Color(0xFFFDD835)
val PriorityLow = Color(0xFF42A5F5)

// App colors
val Primary = Color(0xFF1565C0)
val PrimaryVariant = Color(0xFF0D47A1)
val Secondary = Color(0xFF26A69A)
val Background = Color(0xFFF5F5F5)
val Surface = Color(0xFFFFFFFF)
val Error = Color(0xFFD32F2F)
val OnPrimary = Color(0xFFFFFFFF)
val OnSecondary = Color(0xFFFFFFFF)
val OnBackground = Color(0xFF212121)
val OnSurface = Color(0xFF212121)
val OnError = Color(0xFFFFFFFF)

// Dark theme
val DarkPrimary = Color(0xFF90CAF9)
val DarkPrimaryVariant = Color(0xFF42A5F5)
val DarkSecondary = Color(0xFF80CBC4)
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val DarkOnPrimary = Color(0xFF000000)
val DarkOnSecondary = Color(0xFF000000)
val DarkOnBackground = Color(0xFFE0E0E0)
val DarkOnSurface = Color(0xFFE0E0E0)

fun priorityColor(priority: Int): Color = when (priority) {
    1 -> PriorityCritical
    2 -> PriorityHigh
    3 -> PriorityMedium
    4 -> PriorityLow
    else -> PriorityMedium
}
