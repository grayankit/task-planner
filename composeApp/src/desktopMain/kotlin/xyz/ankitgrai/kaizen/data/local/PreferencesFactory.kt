package xyz.ankitgrai.kaizen.data.local

import java.util.prefs.Preferences

actual class PreferencesFactory {
    private val prefs: Preferences = Preferences.userRoot().node("xyz/ankitgrai/taskplanner")

    actual fun getString(key: String, default: String): String {
        return prefs.get(key, default)
    }

    actual fun putString(key: String, value: String) {
        prefs.put(key, value)
        prefs.flush()
    }
}
