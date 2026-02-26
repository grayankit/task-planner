package com.taskplanner.ui.screen.myday

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.taskplanner.data.repository.AuthRepository
import com.taskplanner.data.repository.CategoryRepository
import com.taskplanner.data.repository.TaskRepository
import com.taskplanner.data.sync.SyncManager
import com.taskplanner.shared.model.CategoryDto
import com.taskplanner.shared.model.TaskDto
import com.taskplanner.ui.components.CategoryItem
import com.taskplanner.ui.components.TaskCard
import com.taskplanner.ui.screen.auth.AuthScreen
import com.taskplanner.ui.screen.categories.CategoriesScreen
import com.taskplanner.ui.screen.taskdetail.TaskDetailScreen
import com.taskplanner.ui.screen.tasklist.TaskListScreen
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                ) {
                    if (todayTasks.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No tasks for today",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Tasks with today's due date will appear here",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                )
                            }
                        }
                    } else {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "${todayTasks.size} task${if (todayTasks.size != 1) "s" else ""} for today",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                        Spacer(Modifier.height(8.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 80.dp),
                        ) {
                            items(todayTasks, key = { it.id }) { task ->
                                TaskCard(
                                    task = task,
                                    onToggleComplete = {
                                        scope.launch { taskRepository.toggleTaskComplete(task.id) }
                                    },
                                    onClick = {
                                        navigator.push(TaskDetailScreen(task.id))
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerContent(
    categories: List<CategoryDto>,
    onMyDayClick: () -> Unit,
    onCategoryClick: (CategoryDto) -> Unit,
    onManageCategories: () -> Unit,
    onLogout: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp),
    ) {
        Text(
            text = "Task Planner",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
        )

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
