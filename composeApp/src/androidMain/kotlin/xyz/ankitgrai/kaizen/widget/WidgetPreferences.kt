package xyz.ankitgrai.kaizen.widget

import android.content.Context
import android.content.SharedPreferences

/**
 * Per-widget preferences for storing the selected list type and opacity.
 * Each widget instance has its own configuration keyed by appWidgetId.
 */
class WidgetPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getListType(appWidgetId: Int): String {
        return prefs.getString(keyListType(appWidgetId), LIST_TYPE_MY_DAY) ?: LIST_TYPE_MY_DAY
    }

    fun setListType(appWidgetId: Int, listType: String) {
        prefs.edit().putString(keyListType(appWidgetId), listType).apply()
    }

    fun getOpacity(appWidgetId: Int): Float {
        return prefs.getFloat(keyOpacity(appWidgetId), DEFAULT_OPACITY)
    }

    fun setOpacity(appWidgetId: Int, opacity: Float) {
        prefs.edit().putFloat(keyOpacity(appWidgetId), opacity).apply()
    }

    fun getCategoryName(appWidgetId: Int): String {
        return prefs.getString(keyCategoryName(appWidgetId), "") ?: ""
    }

    fun setCategoryName(appWidgetId: Int, name: String) {
        prefs.edit().putString(keyCategoryName(appWidgetId), name).apply()
    }

    fun removeWidget(appWidgetId: Int) {
        prefs.edit()
            .remove(keyListType(appWidgetId))
            .remove(keyOpacity(appWidgetId))
            .remove(keyCategoryName(appWidgetId))
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "task_planner_widget_prefs"
        const val LIST_TYPE_MY_DAY = "my_day"
        const val DEFAULT_OPACITY = 0.85f

        private fun keyListType(id: Int): String = "widget_${id}_list_type"
        private fun keyOpacity(id: Int): String = "widget_${id}_opacity"
        private fun keyCategoryName(id: Int): String = "widget_${id}_category_name"
    }
}
