package xyz.ankitgrai.kaizen.data.local

expect class PreferencesFactory {
    fun getString(key: String, default: String): String
    fun putString(key: String, value: String)
}
