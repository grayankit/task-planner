package xyz.ankitgrai.taskplanner.server.routes

import xyz.ankitgrai.taskplanner.server.data.dao.CategoryDao
import xyz.ankitgrai.taskplanner.server.data.dao.DeletedEntityDao
import xyz.ankitgrai.taskplanner.server.data.dao.TaskDao
import xyz.ankitgrai.taskplanner.server.security.getUserId
import xyz.ankitgrai.taskplanner.shared.model.CategoryDto
import xyz.ankitgrai.taskplanner.shared.model.TaskDto
import xyz.ankitgrai.taskplanner.shared.request.SyncPushRequest
import xyz.ankitgrai.taskplanner.shared.response.SyncPullResponse
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun Route.syncRoutes() {
    val taskDao = TaskDao()
    val categoryDao = CategoryDao()
    val deletedEntityDao = DeletedEntityDao()
    val json = Json { ignoreUnknownKeys = true }

    route("/sync") {
        // Pull changes from server since a given timestamp
        get("/pull") {
            val userId = getUserId()
            val sinceParam = call.request.queryParameters["since"]

            val since = if (sinceParam != null) {
                try {
                    LocalDateTime.parse(sinceParam, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                } catch (e: Exception) {
                    LocalDateTime.MIN
                }
            } else {
                LocalDateTime.MIN
            }

            val tasks = taskDao.getModifiedSince(userId, since)
            val categories = categoryDao.getModifiedSince(userId, since)
            val deletedTaskIds = deletedEntityDao.getDeletedSince(userId, "task", since)
            val deletedCategoryIds = deletedEntityDao.getDeletedSince(userId, "category", since)

            call.respond(
                SyncPullResponse(
                    tasks = tasks,
                    categories = categories,
                    deletedTaskIds = deletedTaskIds,
                    deletedCategoryIds = deletedCategoryIds,
                    serverTimestamp = LocalDateTime.now().toString(),
                ),
            )
        }

        // Push local changes to server
        post("/push") {
            val userId = getUserId()
            val request = call.receive<SyncPushRequest>()

            for (op in request.operations) {
                try {
                    when (op.entityType) {
                        "task" -> handleTaskSync(op.operationType, op.entityId, op.payload, userId, taskDao, deletedEntityDao, json)
                        "category" -> handleCategorySync(op.operationType, op.entityId, op.payload, userId, categoryDao, deletedEntityDao, json)
                    }
                } catch (e: Exception) {
                    // Log but continue processing other operations
                    println("Sync error for operation ${op.id}: ${e.message}")
                }
            }

            // Return current server state after push
            val tasks = taskDao.getAllByUser(userId)
            val categories = categoryDao.getAllByUser(userId)

            call.respond(
                SyncPullResponse(
                    tasks = tasks,
                    categories = categories,
                    deletedTaskIds = emptyList(),
                    deletedCategoryIds = emptyList(),
                    serverTimestamp = LocalDateTime.now().toString(),
                ),
            )
        }
    }
}

private suspend fun handleTaskSync(
    operationType: String,
    entityId: String,
    payload: String,
    userId: String,
    taskDao: TaskDao,
    deletedEntityDao: DeletedEntityDao,
    json: Json,
) {
    when (operationType) {
        "CREATE" -> {
            val task = json.decodeFromString<TaskDto>(payload)
            val existing = taskDao.getById(entityId, userId)
            if (existing == null) {
                taskDao.createWithId(
                    id = entityId,
                    userId = userId,
                    categoryId = task.categoryId,
                    title = task.title,
                    description = task.description,
                    priority = task.priority,
                    dueDate = task.dueDate,
                    dueTime = task.dueTime,
                    isCompleted = task.isCompleted,
                )
            }
        }
        "UPDATE" -> {
            val task = json.decodeFromString<TaskDto>(payload)
            taskDao.update(
                id = entityId,
                userId = userId,
                categoryId = task.categoryId,
                title = task.title,
                description = task.description,
                priority = task.priority,
                dueDate = task.dueDate,
                dueTime = task.dueTime,
                isCompleted = task.isCompleted,
            )
        }
        "DELETE" -> {
            taskDao.delete(entityId, userId)
            deletedEntityDao.record(userId, "task", entityId)
        }
    }
}

private suspend fun handleCategorySync(
    operationType: String,
    entityId: String,
    payload: String,
    userId: String,
    categoryDao: CategoryDao,
    deletedEntityDao: DeletedEntityDao,
    json: Json,
) {
    when (operationType) {
        "CREATE" -> {
            val category = json.decodeFromString<CategoryDto>(payload)
            val existing = categoryDao.getById(entityId, userId)
            if (existing == null) {
                categoryDao.createWithId(
                    id = entityId,
                    userId = userId,
                    name = category.name,
                    color = category.color,
                    isDefault = category.isDefault,
                )
            }
        }
        "UPDATE" -> {
            val category = json.decodeFromString<CategoryDto>(payload)
            categoryDao.update(
                id = entityId,
                userId = userId,
                name = category.name,
                color = category.color,
            )
        }
        "DELETE" -> {
            categoryDao.delete(entityId, userId)
            deletedEntityDao.record(userId, "category", entityId)
        }
    }
}
