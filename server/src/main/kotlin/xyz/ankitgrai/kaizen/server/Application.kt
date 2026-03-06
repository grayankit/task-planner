package xyz.ankitgrai.kaizen.server

import xyz.ankitgrai.kaizen.server.data.DatabaseFactory
import xyz.ankitgrai.kaizen.server.plugins.configureAuth
import xyz.ankitgrai.kaizen.server.plugins.configureCORS
import xyz.ankitgrai.kaizen.server.plugins.configureRouting
import xyz.ankitgrai.kaizen.server.plugins.configureSerialization
import xyz.ankitgrai.kaizen.server.plugins.configureStatusPages
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
