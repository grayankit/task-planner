package xyz.ankitgrai.kaizen.ui.screen.myday

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import xyz.ankitgrai.kaizen.data.repository.AuthRepository
import xyz.ankitgrai.kaizen.data.repository.CategoryRepository
import xyz.ankitgrai.kaizen.data.repository.TaskRepository
import xyz.ankitgrai.kaizen.data.sync.SyncManager
import xyz.ankitgrai.kaizen.shared.model.CategoryDto
import xyz.ankitgrai.kaizen.ui.components.CategoryItem
import xyz.ankitgrai.kaizen.ui.components.SectionedTaskList
import xyz.ankitgrai.kaizen.ui.screen.auth.AuthScreen
import xyz.ankitgrai.kaizen.ui.screen.alltasks.AllTasksScreen
import xyz.ankitgrai.kaizen.ui.screen.categories.CategoriesScreen
import xyz.ankitgrai.kaizen.ui.screen.settings.SettingsScreen
import xyz.ankitgrai.kaizen.ui.screen.taskdetail.TaskDetailScreen
import xyz.ankitgrai.kaizen.ui.screen.tasklist.TaskListScreen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

class MyDayScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val taskRepository = koinInject<TaskRepository>()
        val categoryRepository = koinInject<CategoryRepository>()
        val authRepository = koinInject<AuthRepository>()
        val syncManager = koinInject<SyncManager>()
        val scope = rememberCoroutineScope()

        val todayTasks by taskRepository.getTodayTasks().collectAsState(initial = emptyList())
        val categories by categoryRepository.getAllCategories().collectAsState(initial = emptyList())
        val isSyncing by syncManager.isSyncing.collectAsState()

        val drawerState = rememberDrawerState(DrawerValue.Closed)

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    DrawerContent(
                        username = authRepository.getStoredUsername(),
                        categories = categories,
                        onMyDayClick = {
                            scope.launch { drawerState.close() }
                        },
                        onCategoryClick = { category ->
                            scope.launch { drawerState.close() }
                            navigator.push(TaskListScreen(category.id, category.name))
                        },
                        onManageCategories = {
                            scope.launch { drawerState.close() }
                            navigator.push(CategoriesScreen())
                        },
                        onAllTasks = {
                            scope.launch { drawerState.close() }
                            navigator.push(AllTasksScreen())
                        },
                        onSettings = {
                            scope.launch { drawerState.close() }
                            navigator.push(SettingsScreen())
                        },
                        onLogout = {
                            syncManager.stopPeriodicSync()
                            authRepository.logout()
                            navigator.replaceAll(AuthScreen())
                        },
                    )
                }
            },
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("My Day") },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                IconButton(onClick = {
                                    scope.launch {
                                        val token = authRepository.getStoredToken() ?: return@launch
                                        syncManager.sync(token)
                                    }
                                }) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Sync")
                                }
                            }
                        },
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { navigator.push(TaskDetailScreen(null)) },
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Task")
                    }
                },
            ) { paddingValues ->
                SectionedTaskList(
                    tasks = todayTasks,
                    emptyMessage = "No tasks for today",
                    emptySubMessage = "Tasks with today's due date will appear here",
                    onToggleComplete = { task ->
                        scope.launch { taskRepository.toggleTaskComplete(task.id) }
                    },
                    onTaskClick = { task ->
                        navigator.push(TaskDetailScreen(task.id))
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun DrawerContent(
    username: String?,
    categories: List<CategoryDto>,
    onMyDayClick: () -> Unit,
    onCategoryClick: (CategoryDto) -> Unit,
    onManageCategories: () -> Unit,
    onAllTasks: () -> Unit,
    onSettings: () -> Unit,
    onLogout: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp),
    ) {
        Text(
            text = "Kaizen",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
        )

        if (username != null) {
            Text(
                text = "@$username",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }

        Spacer(Modifier.height(24.dp))

        // My Day (always first)
        CategoryItem(
            name = "My Day",
            color = "#FDD835",
            taskCount = 0,
            isSelected = true,
            onClick = onMyDayClick,
        )

        Spacer(Modifier.height(8.dp))

        HorizontalDivider()

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Categories",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(Modifier.height(4.dp))

        categories.forEach { category ->
            CategoryItem(
                name = category.name,
                color = category.color,
                taskCount = 0,
                isSelected = false,
                onClick = { onCategoryClick(category) },
            )
        }

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = onManageCategories,
            modifier = Modifier.padding(horizontal = 8.dp),
        ) {
            Text("Manage Categories")
        }

        Spacer(Modifier.height(4.dp))

        TextButton(
            onClick = onAllTasks,
            modifier = Modifier.padding(horizontal = 8.dp),
        ) {
            Text("All Tasks")
        }

        Spacer(Modifier.height(4.dp))

        TextButton(
            onClick = onSettings,
            modifier = Modifier.padding(horizontal = 8.dp),
        ) {
            Text("Settings")
        }

        Spacer(Modifier.weight(1f))

        HorizontalDivider()

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = onLogout,
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.error,
            ),
        ) {
            Text("Sign Out")
        }
    }
}
