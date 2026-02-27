package xyz.ankitgrai.taskplanner.server.routes

import xyz.ankitgrai.taskplanner.server.data.dao.CategoryDao
import xyz.ankitgrai.taskplanner.server.data.dao.DeletedEntityDao
import xyz.ankitgrai.taskplanner.server.security.getUserId
import xyz.ankitgrai.taskplanner.shared.request.CreateCategoryRequest
import xyz.ankitgrai.taskplanner.shared.request.UpdateCategoryRequest
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.categoryRoutes() {
    val categoryDao = CategoryDao()
    val deletedEntityDao = DeletedEntityDao()

    route("/categories") {
        get {
            val userId = getUserId()
            val categories = categoryDao.getAllByUser(userId)
            call.respond(categories)
        }

        get("/{id}") {
            val userId = getUserId()
            val categoryId = call.parameters["id"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing category ID"),
            )

            val category = categoryDao.getById(categoryId, userId)
            if (category != null) {
                call.respond(category)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Category not found"))
            }
        }

        post {
            val userId = getUserId()
            val request = call.receive<CreateCategoryRequest>()

            if (request.name.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Category name is required"))
                return@post
            }

            // Check for duplicate name (case-insensitive)
            val existing = categoryDao.findByNameAndUserId(request.name, userId)
            if (existing != null) {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to "A category with this name already exists"))
                return@post
            }

            val category = categoryDao.create(
                userId = userId,
                name = request.name,
                color = request.color,
            )

            call.respond(HttpStatusCode.Created, category)
        }

        put("/{id}") {
            val userId = getUserId()
            val categoryId = call.parameters["id"] ?: return@put call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing category ID"),
            )
            val request = call.receive<UpdateCategoryRequest>()

            // Check for duplicate name (case-insensitive), excluding current category
            if (request.name != null) {
                val existing = categoryDao.findByNameAndUserId(request.name!!, userId)
                if (existing != null && existing.id != categoryId) {
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to "A category with this name already exists"))
                    return@put
                }
            }

            val category = categoryDao.update(
                id = categoryId,
                userId = userId,
                name = request.name,
                color = request.color,
            )

            if (category != null) {
                call.respond(category)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Category not found"))
            }
        }

        delete("/{id}") {
            val userId = getUserId()
            val categoryId = call.parameters["id"] ?: return@delete call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing category ID"),
            )

            val deleted = categoryDao.delete(categoryId, userId)
            if (deleted) {
                deletedEntityDao.record(userId, "category", categoryId)
                call.respond(HttpStatusCode.OK, mapOf("message" to "Category deleted"))
            } else {
                call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Category not found or cannot delete default category"),
                )
            }
        }
    }
}
