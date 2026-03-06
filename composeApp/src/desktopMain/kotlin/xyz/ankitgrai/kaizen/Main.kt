package xyz.ankitgrai.kaizen

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import xyz.ankitgrai.kaizen.data.local.DriverFactory
import xyz.ankitgrai.kaizen.data.local.PreferencesFactory
import xyz.ankitgrai.kaizen.di.appModule
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
            "Kaizen is already running.",
            "Kaizen",
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
            title = "Kaizen",
            state = rememberWindowState(width = 900.dp, height = 700.dp),
        ) {
            App()
        }
    }
}
