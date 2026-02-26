package xyz.ankitgrai.taskplanner.di

import xyz.ankitgrai.taskplanner.data.local.DriverFactory
import xyz.ankitgrai.taskplanner.data.remote.ApiService
import xyz.ankitgrai.taskplanner.data.remote.HttpClientFactory
import xyz.ankitgrai.taskplanner.data.repository.AuthRepository
import xyz.ankitgrai.taskplanner.data.repository.CategoryRepository
import xyz.ankitgrai.taskplanner.data.repository.TaskRepository
import xyz.ankitgrai.taskplanner.data.sync.SyncEnqueuer
import xyz.ankitgrai.taskplanner.data.sync.SyncManager
import xyz.ankitgrai.taskplanner.db.TaskPlannerDatabase
import org.koin.dsl.module

// Base URL configurable via env or hardcoded for dev
const val DEFAULT_BASE_URL = "https://task-planner-api-vhkm.onrender.com"

fun appModule(driverFactory: DriverFactory) = module {
    // Database
    single<TaskPlannerDatabase> {
        TaskPlannerDatabase(driverFactory.createDriver())
    }

    // HTTP Client
    single { HttpClientFactory.create() }

    // API Service
    single {
        val baseUrl = getPropertyOrNull<String>("SERVER_URL") ?: DEFAULT_BASE_URL
        ApiService(get(), baseUrl)
    }

    // Sync
    single { SyncEnqueuer(get()) }

    // Repositories
    single { AuthRepository(get(), get()) }
    single { TaskRepository(get(), get()) }
    single { CategoryRepository(get(), get()) }

    // Sync Manager
    single { SyncManager(get(), get(), get(), get()) }
}
