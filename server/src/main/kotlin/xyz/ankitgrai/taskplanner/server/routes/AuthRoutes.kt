package xyz.ankitgrai.taskplanner.server.routes

import xyz.ankitgrai.taskplanner.server.data.dao.CategoryDao
import xyz.ankitgrai.taskplanner.server.data.dao.UserDao
import xyz.ankitgrai.taskplanner.server.security.JwtConfig
import xyz.ankitgrai.taskplanner.server.security.PasswordHasher
import xyz.ankitgrai.taskplanner.shared.request.AuthRequest
import xyz.ankitgrai.taskplanner.shared.response.AuthResponse
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes() {
    val userDao = UserDao()
    val categoryDao = CategoryDao()

    route("/auth") {
        post("/register") {
            val request = call.receive<AuthRequest>()

            // Validate invite code
            val requiredInviteCode = System.getenv("INVITE_CODE")
            if (!requiredInviteCode.isNullOrBlank()) {
                if (request.inviteCode.isNullOrBlank() || request.inviteCode != requiredInviteCode) {
                    call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Invalid invite code"))
                    return@post
                }
            }

            if (request.username.length < 3) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Username must be at least 3 characters"))
                return@post
            }
            if (request.password.length < 6) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Password must be at least 6 characters"))
                return@post
            }

            val existing = userDao.findByUsername(request.username)
            if (existing != null) {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to "Username already taken"))
                return@post
            }

            val passwordHash = PasswordHasher.hash(request.password)
            val user = userDao.create(request.username, passwordHash)
            val token = JwtConfig.generateToken(user.id, user.username)

            // Create default "General" category for new user
            categoryDao.create(
                userId = user.id,
                name = "General",
                color = "#42A5F5",
                isDefault = true,
            )

            call.respond(
                HttpStatusCode.Created,
                AuthResponse(token = token, userId = user.id, username = user.username),
            )
        }

        post("/login") {
            val request = call.receive<AuthRequest>()

            val user = userDao.findByUsername(request.username)
            if (user == null) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid credentials"))
                return@post
            }

            val passwordHash = userDao.getPasswordHash(request.username)
            if (passwordHash == null || !PasswordHasher.verify(request.password, passwordHash)) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid credentials"))
                return@post
            }

            val token = JwtConfig.generateToken(user.id, user.username)
            call.respond(AuthResponse(token = token, userId = user.id, username = user.username))
        }
    }
}
