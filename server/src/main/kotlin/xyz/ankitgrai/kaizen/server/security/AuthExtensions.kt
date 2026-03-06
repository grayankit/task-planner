package xyz.ankitgrai.kaizen.server.security

import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*

fun RoutingContext.getUserId(): String {
    val principal = call.principal<JWTPrincipal>()
        ?: throw IllegalStateException("No JWT principal found")
    return principal.payload.getClaim("userId").asString()
}
