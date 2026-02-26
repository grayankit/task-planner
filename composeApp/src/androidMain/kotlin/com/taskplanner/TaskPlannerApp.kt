package com.taskplanner

import android.app.Application
import com.taskplanner.data.local.DriverFactory
import com.taskplanner.di.appModule
import org.koin.core.context.startKoin

class TaskPlannerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(appModule(DriverFactory(this@TaskPlannerApp)))
        }
    }
}
