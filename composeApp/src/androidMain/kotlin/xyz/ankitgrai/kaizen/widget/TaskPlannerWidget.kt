package xyz.ankitgrai.kaizen.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.provideContent
import androidx.glance.action.ActionParameters
import androidx.glance.currentState
import androidx.glance.layout.fillMaxSize

class TaskPlannerWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val widgetPrefs = WidgetPreferences(context)
        val dataProvider = WidgetDataProvider(context)

        val listType = widgetPrefs.getListType(appWidgetId)
        val opacity = widgetPrefs.getOpacity(appWidgetId)

        val title: String
        val tasks: List<WidgetTask>

        if (listType == WidgetPreferences.LIST_TYPE_MY_DAY) {
            title = "My Day"
            tasks = dataProvider.getMyDayTasks()
        } else {
            // listType is a category ID
            val categoryName = widgetPrefs.getCategoryName(appWidgetId)
            title = categoryName.ifEmpty { "Tasks" }
            tasks = dataProvider.getTasksByCategory(listType)
        }

        provideContent {
            TaskWidgetContent(
                title = title,
                tasks = tasks,
                opacity = opacity,
                appWidgetId = appWidgetId,
            )
        }
    }

    companion object {
        val PARAM_TASK_ID = ActionParameters.Key<String>("task_id")
        val PARAM_COMPLETED = ActionParameters.Key<String>("completed")
        val PARAM_WIDGET_ID = ActionParameters.Key<String>("widget_id")
    }
}
