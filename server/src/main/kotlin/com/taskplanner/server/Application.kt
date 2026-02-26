package com.taskplanner.server

import com.taskplanner.server.data.DatabaseFactory
import com.taskplanner.server.plugins.configureAuth
import com.taskplanner.server.plugins.configureCORS
import com.taskplanner.server.plugins.configureRouting
import com.taskplanner.server.plugins.configureSerialization
import com.taskplanner.server.plugins.configureStatusPages
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(
        Netty,
        port = port,
        host = "0.0.0.0",
        module = Application::module,
    ).start(wait = true)
}

fun Application.module() {
    DatabaseFactory.init()
    configureSerialization()
    configureStatusPages()
    configureCORS()
    configureAuth()
    configureRouting()
}
