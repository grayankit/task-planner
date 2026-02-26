package com.taskplanner.ui.screen.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.taskplanner.data.repository.AuthRepository
import com.taskplanner.data.sync.SyncManager
import com.taskplanner.ui.screen.myday.MyDayScreen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

class AuthScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val authRepository = koinInject<AuthRepository>()
        val syncManager = koinInject<SyncManager>()
        val scope = rememberCoroutineScope()

        var isLogin by remember { mutableStateOf(true) }
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var inviteCode by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }
        var isLoading by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Task Planner",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = if (isLogin) "Sign in to your account" else "Create a new account",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )

            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it; error = null },
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.8f),
                shape = RoundedCornerShape(12.dp),
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; error = null },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(0.8f),
                shape = RoundedCornerShape(12.dp),
            )

            if (!isLogin) {
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = inviteCode,
                    onValueChange = { inviteCode = it; error = null },
                    label = { Text("Invite Code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(0.8f),
                    shape = RoundedCornerShape(12.dp),
                )
            }

            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        error = null
                        val result = if (isLogin) {
                            authRepository.login(username, password)
                        } else {
                            authRepository.register(username, password, inviteCode)
                        }

                        result.fold(
                            onSuccess = { response ->
                                // Start sync after login
                                syncManager.sync(response.token)
                                syncManager.startPeriodicSync(response.token)
                                navigator.replaceAll(MyDayScreen())
                            },
                            onFailure = { e ->
                                error = e.message ?: "An error occurred"
                            },
                        )
                        isLoading = false
                    }
                },
                enabled = username.isNotBlank() && password.isNotBlank() && !isLoading,
                modifier = Modifier.fillMaxWidth(0.8f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(if (isLogin) "Sign In" else "Sign Up")
                }
            }

            Spacer(Modifier.height(16.dp))

            TextButton(
                onClick = {
                    isLogin = !isLogin
                    error = null
                },
            ) {
                Text(
                    if (isLogin) "Don't have an account? Sign up" else "Already have an account? Sign in",
                )
            }
        }
    }
}
