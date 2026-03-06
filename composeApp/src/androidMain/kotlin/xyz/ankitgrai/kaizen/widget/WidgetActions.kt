package xyz.ankitgrai.kaizen.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback

class ToggleTaskAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val taskId = parameters[TaskPlannerWidget.PARAM_TASK_ID] ?: return
        val completed = parameters[TaskPlannerWidget.PARAM_COMPLETED]?.toBooleanStrictOrNull() ?: return

        val dataProvider = WidgetDataProvider(context)
        dataProvider.toggleTaskComplete(taskId, completed)

        // Refresh the widget to reflect the change
        TaskPlannerWidget().update(context, glanceId)
    }
}

class RefreshWidgetAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        TaskPlannerWidget().update(context, glanceId)
    }
}
