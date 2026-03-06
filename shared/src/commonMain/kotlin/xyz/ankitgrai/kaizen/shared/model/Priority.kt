package xyz.ankitgrai.kaizen.shared.model

import kotlinx.serialization.Serializable

@Serializable
enum class Priority(val value: Int, val label: String, val hexColor: String) {
    CRITICAL(1, "Critical", "#E53935"),
    HIGH(2, "High", "#FB8C00"),
    MEDIUM(3, "Medium", "#FDD835"),
    LOW(4, "Low", "#42A5F5");

    companion object {
        fun fromValue(value: Int): Priority = entries.first { it.value == value }
    }
}
