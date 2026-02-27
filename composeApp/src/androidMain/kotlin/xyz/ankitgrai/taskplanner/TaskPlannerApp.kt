package xyz.ankitgrai.taskplanner

import android.app.Application
import xyz.ankitgrai.taskplanner.data.local.DriverFactory
import xyz.ankitgrai.taskplanner.data.local.PreferencesFactory
import xyz.ankitgrai.taskplanner.di.appModule
import org.koin.core.context.startKoin

class TaskPlannerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(
                appModule(
                    DriverFactory(this@TaskPlannerApp),
                    PreferencesFactory(this@TaskPlannerApp),
                )
            )
        }
    }
}
