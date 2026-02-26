package xyz.ankitgrai.taskplanner

import androidx.compose.runtime.*
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import xyz.ankitgrai.taskplanner.data.repository.AuthRepository
import xyz.ankitgrai.taskplanner.data.sync.SyncManager
import xyz.ankitgrai.taskplanner.ui.screen.auth.AuthScreen
import xyz.ankitgrai.taskplanner.ui.screen.myday.MyDayScreen
import xyz.ankitgrai.taskplanner.ui.theme.TaskPlannerTheme
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
