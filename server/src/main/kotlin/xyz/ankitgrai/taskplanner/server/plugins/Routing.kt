package xyz.ankitgrai.taskplanner.server.plugins

import xyz.ankitgrai.taskplanner.server.routes.authRoutes
import xyz.ankitgrai.taskplanner.server.routes.categoryRoutes
import xyz.ankitgrai.taskplanner.server.routes.syncRoutes
import xyz.ankitgrai.taskplanner.server.routes.taskRoutes
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respond(mapOf("status" to "Task Planner API is running"))
        }

        route("/api") {
            authRoutes()

            authenticate("auth-jwt") {
                taskRoutes()
                categoryRoutes()
                syncRoutes()
            }
        }
    }
}
