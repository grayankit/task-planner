package xyz.ankitgrai.taskplanner.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import xyz.ankitgrai.taskplanner.db.TaskPlannerDatabase
import java.io.File

actual class DriverFactory {
    actual fun createDriver(): SqlDriver {
        val dbDir = File(System.getProperty("user.home"), ".taskplanner")
        dbDir.mkdirs()
        val dbFile = File(dbDir, "taskplanner.db")
        val dbExists = dbFile.exists()
        val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
        if (!dbExists) {
            TaskPlannerDatabase.Schema.create(driver)
        }
        return driver
    }
}
