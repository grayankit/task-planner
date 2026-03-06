package xyz.ankitgrai.kaizen

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import xyz.ankitgrai.kaizen.data.repository.AuthRepository
import xyz.ankitgrai.kaizen.data.sync.SyncManager
import xyz.ankitgrai.kaizen.data.update.UpdateChecker
import xyz.ankitgrai.kaizen.ui.screen.auth.AuthScreen
import xyz.ankitgrai.kaizen.ui.screen.myday.MyDayScreen
import xyz.ankitgrai.kaizen.ui.theme.TaskPlannerTheme
import xyz.ankitgrai.kaizen.util.getAppVersion
import xyz.ankitgrai.kaizen.util.isAndroid
import org.koin.compose.koinInject

@Composable
fun App() {
    TaskPlannerTheme {
        val authRepository = koinInject<AuthRepository>()
        val syncManager = koinInject<SyncManager>()
        val updateChecker = koinInject<UpdateChecker>()
        val snackbarHostState = remember { SnackbarHostState() }

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

        // Check for updates on launch (Android only)
        if (isAndroid()) {
            LaunchedEffect(Unit) {
                val result = updateChecker.checkForUpdate(getAppVersion())
                if (result != null && result.isUpdateAvailable) {
                    snackbarHostState.showSnackbar(
                        message = "Update available: v${result.latestVersion}",
                        actionLabel = "Details",
                        duration = SnackbarDuration.Long,
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Navigator(startScreen) { navigator ->
                SlideTransition(navigator)
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
            )
        }
    }
}
