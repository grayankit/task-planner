package xyz.ankitgrai.kaizen.data.local

import android.content.Context
import android.content.SharedPreferences

actual class PreferencesFactory(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("task_planner_prefs", Context.MODE_PRIVATE)

    actual fun getString(key: String, default: String): String {
        return prefs.getString(key, default) ?: default
    }

    actual fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }
}
