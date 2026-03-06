package xyz.ankitgrai.kaizen.di

import xyz.ankitgrai.kaizen.data.local.DriverFactory
import xyz.ankitgrai.kaizen.data.local.PreferencesFactory
import xyz.ankitgrai.kaizen.data.remote.ApiService
import xyz.ankitgrai.kaizen.data.remote.HttpClientFactory
import xyz.ankitgrai.kaizen.data.repository.AuthRepository
import xyz.ankitgrai.kaizen.data.repository.CategoryRepository
import xyz.ankitgrai.kaizen.data.repository.TaskRepository
import xyz.ankitgrai.kaizen.data.sync.SyncEnqueuer
import xyz.ankitgrai.kaizen.data.sync.SyncManager
import xyz.ankitgrai.kaizen.data.update.UpdateChecker
import xyz.ankitgrai.kaizen.db.TaskPlannerDatabase
import xyz.ankitgrai.kaizen.ui.theme.ThemeManager
import org.koin.dsl.module

// Base URL configurable via env or hardcoded for dev
const val DEFAULT_BASE_URL = "https://task-planner-api-vhkm.onrender.com"

fun appModule(driverFactory: DriverFactory, preferencesFactory: PreferencesFactory) = module {
    // Database
    single<TaskPlannerDatabase> {
        TaskPlannerDatabase(driverFactory.createDriver())
    }

    // Preferences
    single<PreferencesFactory> { preferencesFactory }

    // Theme
    single { ThemeManager(get()) }

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

    // Update Checker
    single { UpdateChecker(get()) }
}
