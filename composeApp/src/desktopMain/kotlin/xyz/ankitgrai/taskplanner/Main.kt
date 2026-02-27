package xyz.ankitgrai.taskplanner

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import xyz.ankitgrai.taskplanner.data.local.DriverFactory
import xyz.ankitgrai.taskplanner.data.local.PreferencesFactory
import xyz.ankitgrai.taskplanner.di.appModule
import org.koin.core.context.startKoin
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import javax.swing.JOptionPane
import kotlin.system.exitProcess

fun main() {
    // Single-instance enforcement via file lock
    val lockFile = Path.of(
        System.getProperty("user.home"), ".local", "share", "TaskPlanner", "taskplanner.lock"
    )
    Files.createDirectories(lockFile.parent)
    val channel = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE)
    val lock = channel.tryLock()
    if (lock == null) {
        JOptionPane.showMessageDialog(
            null,
            "Task Planner is already running.",
            "Task Planner",
            JOptionPane.INFORMATION_MESSAGE,
        )
        channel.close()
        exitProcess(1)
    }

    startKoin {
        modules(
            appModule(
                DriverFactory(),
                PreferencesFactory(),
            )
        )
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
