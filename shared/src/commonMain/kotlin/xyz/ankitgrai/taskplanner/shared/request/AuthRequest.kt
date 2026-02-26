package xyz.ankitgrai.taskplanner.shared.request

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(
    val username: String,
    val password: String,
    val inviteCode: String? = null,
)
