package xyz.ankitgrai.kaizen.widget

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import xyz.ankitgrai.kaizen.db.TaskPlannerDatabase
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Simple data class representing a task for the widget display.
 */
data class WidgetTask(
    val id: String,
    val title: String,
    val priority: Int,
    val isCompleted: Boolean,
    val dueTime: String?,
)

/**
 * Simple data class representing a category for widget configuration.
 */
data class WidgetCategory(
    val id: String,
    val name: String,
    val color: String?,
    val isDefault: Boolean,
)

/**
 * Provides data to the widget by directly accessing the SQLDelight database.
 * This avoids depending on Koin, which may not be initialized when the widget runs.
 */
class WidgetDataProvider(private val context: Context) {

    private fun getDatabase(): TaskPlannerDatabase {
        val driver = AndroidSqliteDriver(TaskPlannerDatabase.Schema, context, "taskplanner.db")
        return TaskPlannerDatabase(driver)
    }

    fun getMyDayTasks(): List<WidgetTask> {
        val db = getDatabase()
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        return db.taskQueries.getTasksByDueDate(today).executeAsList().map { entity ->
            WidgetTask(
                id = entity.id,
                title = entity.title,
                priority = entity.priority.toInt(),
                isCompleted = entity.is_completed != 0L,
                dueTime = entity.due_time,
            )
        }
    }

    fun getTasksByCategory(categoryId: String): List<WidgetTask> {
        val db = getDatabase()
        return db.taskQueries.getTasksByCategory(categoryId).executeAsList().map { entity ->
            WidgetTask(
                id = entity.id,
                title = entity.title,
                priority = entity.priority.toInt(),
                isCompleted = entity.is_completed != 0L,
                dueTime = entity.due_time,
            )
        }
    }

    fun getCategories(): List<WidgetCategory> {
        val db = getDatabase()
        return db.categoryQueries.getAllCategories().executeAsList().map { entity ->
            WidgetCategory(
                id = entity.id,
                name = entity.name,
                color = entity.color,
                isDefault = entity.is_default != 0L,
            )
        }
    }

    fun toggleTaskComplete(taskId: String, completed: Boolean) {
        val db = getDatabase()
        val now = java.time.Instant.now().toString()
        val completedAt = if (completed) now else null
        db.taskQueries.toggleComplete(
            is_completed = if (completed) 1L else 0L,
            completed_at = completedAt,
            updated_at = now,
            id = taskId,
        )
    }
}
