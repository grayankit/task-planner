package xyz.ankitgrai.taskplanner.shared.response

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String,
    val userId: String,
    val username: String,
)
