package xyz.ankitgrai.taskplanner

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import xyz.ankitgrai.taskplanner.data.local.DriverFactory
import xyz.ankitgrai.taskplanner.di.appModule
import org.koin.core.context.startKoin

fun main() {
    startKoin {
        modules(appModule(DriverFactory()))
    }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Task Planner",
            state = rememberWindowState(width = 900.dp, height = 700.dp),
        ) {
            App()
        }
    }
}
