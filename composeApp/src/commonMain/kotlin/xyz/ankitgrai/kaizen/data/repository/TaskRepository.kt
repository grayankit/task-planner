package xyz.ankitgrai.kaizen.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import xyz.ankitgrai.kaizen.data.sync.SyncEnqueuer
import xyz.ankitgrai.kaizen.db.TaskPlannerDatabase
import xyz.ankitgrai.kaizen.shared.model.TaskDto
import com.benasher44.uuid.uuid4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class TaskRepository(
    private val database: TaskPlannerDatabase,
    private val syncEnqueuer: SyncEnqueuer,
) {
    fun getAllTasks(): Flow<List<TaskDto>> {
        return database.taskQueries.getAllTasks()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { entities -> entities.map { it.toDto() } }
    }

    fun getTasksByCategory(categoryId: String): Flow<List<TaskDto>> {
        return database.taskQueries.getTasksByCategory(categoryId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { entities -> entities.map { it.toDto() } }
    }

    fun getTodayTasks(): Flow<List<TaskDto>> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        return database.taskQueries.getTasksByDueDate(today)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { entities -> entities.map { it.toDto() } }
    }

    fun getTaskById(id: String): Flow<TaskDto?> {
        return database.taskQueries.getTaskById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { it?.toDto() }
    }

    fun getTaskCount(): Long {
        return database.taskQueries.getTaskCount().executeAsOne()
    }

    fun getTasksPaginated(limit: Long, offset: Long): List<TaskDto> {
        return database.taskQueries.getAllTasksPaginated(limit, offset)
            .executeAsList()
            .map { it.toDto() }
    }

    fun createTask(
        categoryId: String?,
        title: String,
        description: String?,
        priority: Int,
        dueDate: String?,
        dueTime: String?,
    ): TaskDto {
        val id = uuid4().toString()
        val now = Clock.System.now().toString()
        val userId = "" // Will be set by server on sync

        // Fall back to default category if none selected
        val resolvedCategoryId = categoryId
            ?: database.categoryQueries.getDefaultCategory().executeAsOneOrNull()?.id

        val task = TaskDto(
            id = id,
            userId = userId,
            categoryId = resolvedCategoryId,
            title = title,
            description = description,
            priority = priority,
            dueDate = dueDate,
            dueTime = dueTime,
            isCompleted = false,
            createdAt = now,
            updatedAt = now,
        )

        database.taskQueries.insertTask(
            id = task.id,
            user_id = task.userId,
            category_id = task.categoryId,
            title = task.title,
            description = task.description,
            priority = task.priority.toLong(),
            due_date = task.dueDate,
            due_time = task.dueTime,
            is_completed = 0,
            completed_at = null,
            created_at = task.createdAt,
            updated_at = task.updatedAt,
        )

        syncEnqueuer.enqueueCreate("task", id, task)
        return task
    }

    fun updateTask(
        id: String,
        categoryId: String?,
        title: String,
        description: String?,
        priority: Int,
        dueDate: String?,
        dueTime: String?,
    ): TaskDto? {
        val now = Clock.System.now().toString()
        val existing = database.taskQueries.getTaskById(id).executeAsOneOrNull() ?: return null

        database.taskQueries.updateTask(
            category_id = categoryId,
            title = title,
            description = description,
            priority = priority.toLong(),
            due_date = dueDate,
            due_time = dueTime,
            is_completed = existing.is_completed,
            completed_at = existing.completed_at,
            updated_at = now,
            id = id,
        )

        val updated = existing.toDto().copy(
            categoryId = categoryId,
            title = title,
            description = description,
            priority = priority,
            dueDate = dueDate,
            dueTime = dueTime,
            updatedAt = now,
        )

        syncEnqueuer.enqueueUpdate("task", id, updated)
        return updated
    }

    fun toggleTaskComplete(id: String) {
        val existing = database.taskQueries.getTaskById(id).executeAsOneOrNull() ?: return
        val now = Clock.System.now().toString()
        val newCompleted = if (existing.is_completed == 0L) 1L else 0L
        val completedAt = if (newCompleted == 1L) now else null

        database.taskQueries.toggleComplete(
            is_completed = newCompleted,
            completed_at = completedAt,
            updated_at = now,
            id = id,
        )

        val updated = existing.toDto().copy(
            isCompleted = newCompleted == 1L,
            completedAt = completedAt,
            updatedAt = now,
        )
        syncEnqueuer.enqueueUpdate("task", id, updated)
    }

    fun deleteTask(id: String) {
        database.taskQueries.deleteTask(id)
        syncEnqueuer.enqueueDelete("task", id)
    }

    fun replaceAllTasks(tasks: List<TaskDto>) {
        database.taskQueries.deleteAllTasks()
        tasks.forEach { task ->
            database.taskQueries.insertTask(
                id = task.id,
                user_id = task.userId,
                category_id = task.categoryId,
                title = task.title,
                description = task.description,
                priority = task.priority.toLong(),
                due_date = task.dueDate,
                due_time = task.dueTime,
                is_completed = if (task.isCompleted) 1L else 0L,
                completed_at = task.completedAt,
                created_at = task.createdAt,
                updated_at = task.updatedAt,
            )
        }
    }

    fun upsertTask(task: TaskDto) {
        database.taskQueries.insertTask(
            id = task.id,
            user_id = task.userId,
            category_id = task.categoryId,
            title = task.title,
            description = task.description,
            priority = task.priority.toLong(),
            due_date = task.dueDate,
            due_time = task.dueTime,
            is_completed = if (task.isCompleted) 1L else 0L,
            completed_at = task.completedAt,
            created_at = task.createdAt,
            updated_at = task.updatedAt,
        )
    }
}

private fun xyz.ankitgrai.kaizen.db.TaskEntity.toDto() = TaskDto(
    id = id,
    userId = user_id,
    categoryId = category_id,
    title = title,
    description = description,
    priority = priority.toInt(),
    dueDate = due_date,
    dueTime = due_time,
    isCompleted = is_completed != 0L,
    completedAt = completed_at,
    createdAt = created_at,
    updatedAt = updated_at,
)
