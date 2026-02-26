package com.taskplanner.server.plugins

import com.taskplanner.server.routes.authRoutes
import com.taskplanner.server.routes.categoryRoutes
import com.taskplanner.server.routes.syncRoutes
import com.taskplanner.server.routes.taskRoutes
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
