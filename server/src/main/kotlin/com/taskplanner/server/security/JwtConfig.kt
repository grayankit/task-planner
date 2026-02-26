package com.taskplanner.server.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

object JwtConfig {
    private val secret: String get() = System.getenv("JWT_SECRET") ?: "dev-secret-change-in-production"
    private val issuer: String get() = System.getenv("JWT_ISSUER") ?: "task-planner"
    private val audience: String get() = System.getenv("JWT_AUDIENCE") ?: "task-planner-users"
    private const val VALIDITY_MS = 7L * 24 * 60 * 60 * 1000 // 7 days

    fun generateToken(userId: String, username: String): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("userId", userId)
            .withClaim("username", username)
            .withExpiresAt(Date(System.currentTimeMillis() + VALIDITY_MS))
            .sign(Algorithm.HMAC256(secret))
    }
}
