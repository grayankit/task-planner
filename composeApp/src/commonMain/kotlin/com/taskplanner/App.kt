package com.taskplanner

import androidx.compose.runtime.*
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.taskplanner.data.repository.AuthRepository
import com.taskplanner.data.sync.SyncManager
import com.taskplanner.ui.screen.auth.AuthScreen
import com.taskplanner.ui.screen.myday.MyDayScreen
import com.taskplanner.ui.theme.TaskPlannerTheme
import org.koin.compose.koinInject

@Composable
fun App() {
    TaskPlannerTheme {
        val authRepository = koinInject<AuthRepository>()
        val syncManager = koinInject<SyncManager>()

        val startScreen = if (authRepository.isLoggedIn()) {
            // Start periodic sync if already logged in
            LaunchedEffect(Unit) {
                val token = authRepository.getStoredToken()
                if (token != null) {
                    syncManager.sync(token)
                    syncManager.startPeriodicSync(token)
                }
            }
            MyDayScreen()
        } else {
            AuthScreen()
        }

        Navigator(startScreen) { navigator ->
            SlideTransition(navigator)
        }
    }
}
