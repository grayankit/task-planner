package xyz.ankitgrai.kaizen

import android.app.Application
import xyz.ankitgrai.kaizen.data.local.DriverFactory
import xyz.ankitgrai.kaizen.data.local.PreferencesFactory
import xyz.ankitgrai.kaizen.di.appModule
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
