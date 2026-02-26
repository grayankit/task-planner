package xyz.ankitgrai.taskplanner.server.routes

import xyz.ankitgrai.taskplanner.server.data.dao.DeletedEntityDao
import xyz.ankitgrai.taskplanner.server.data.dao.TaskDao
import xyz.ankitgrai.taskplanner.server.security.getUserId
import xyz.ankitgrai.taskplanner.shared.request.CreateTaskRequest
import xyz.ankitgrai.taskplanner.shared.request.UpdateTaskRequest
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.taskRoutes() {
    val taskDao = TaskDao()
    val deletedEntityDao = DeletedEntityDao()

    route("/tasks") {
        get {
            val userId = getUserId()
            val categoryId = call.request.queryParameters["category_id"]

            val tasks = if (categoryId != null) {
                taskDao.getByCategory(userId, categoryId)
            } else {
                taskDao.getAllByUser(userId)
            }

            call.respond(tasks)
        }

        get("/{id}") {
            val userId = getUserId()
            val taskId = call.parameters["id"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing task ID"),
            )

            val task = taskDao.getById(taskId, userId)
            if (task != null) {
                call.respond(task)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Task not found"))
            }
        }

        post {
            val userId = getUserId()
            val request = call.receive<CreateTaskRequest>()

            if (request.title.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Title is required"))
                return@post
            }

            val task = taskDao.create(
                userId = userId,
                categoryId = request.categoryId,
                title = request.title,
                description = request.description,
                priority = request.priority,
                dueDate = request.dueDate,
                dueTime = request.dueTime,
            )

            call.respond(HttpStatusCode.Created, task)
        }

        put("/{id}") {
            val userId = getUserId()
            val taskId = call.parameters["id"] ?: return@put call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing task ID"),
            )
            val request = call.receive<UpdateTaskRequest>()

            val task = taskDao.update(
                id = taskId,
                userId = userId,
                categoryId = request.categoryId,
                title = request.title,
                description = request.description,
                priority = request.priority,
                dueDate = request.dueDate,
                dueTime = request.dueTime,
                isCompleted = request.isCompleted,
            )

            if (task != null) {
                call.respond(task)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Task not found"))
            }
        }

        delete("/{id}") {
            val userId = getUserId()
            val taskId = call.parameters["id"] ?: return@delete call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing task ID"),
            )

            val deleted = taskDao.delete(taskId, userId)
            if (deleted) {
                deletedEntityDao.record(userId, "task", taskId)
                call.respond(HttpStatusCode.OK, mapOf("message" to "Task deleted"))
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Task not found"))
            }
        }
    }
}
