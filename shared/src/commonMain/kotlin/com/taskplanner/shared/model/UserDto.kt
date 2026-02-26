package com.taskplanner.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String = "",
    val username: String,
    val createdAt: String = "",
)
