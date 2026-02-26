package com.taskplanner.data.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.taskplanner.db.TaskPlannerDatabase

actual class DriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(TaskPlannerDatabase.Schema, context, "taskplanner.db")
    }
}
