package com.taskplanner.di

import com.taskplanner.data.local.DriverFactory
import com.taskplanner.data.remote.ApiService
import com.taskplanner.data.remote.HttpClientFactory
import com.taskplanner.data.repository.AuthRepository
import com.taskplanner.data.repository.CategoryRepository
import com.taskplanner.data.repository.TaskRepository
import com.taskplanner.data.sync.SyncEnqueuer
import com.taskplanner.data.sync.SyncManager
import com.taskplanner.db.TaskPlannerDatabase
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
