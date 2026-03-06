package xyz.ankitgrai.kaizen.server.plugins

import xyz.ankitgrai.kaizen.server.routes.authRoutes
import xyz.ankitgrai.kaizen.server.routes.categoryRoutes
import xyz.ankitgrai.kaizen.server.routes.syncRoutes
import xyz.ankitgrai.kaizen.server.routes.taskRoutes
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respond(mapOf("status" to "Kaizen API is running"))
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
