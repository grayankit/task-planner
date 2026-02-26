package xyz.ankitgrai.taskplanner.ui.screen.tasklist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import xyz.ankitgrai.taskplanner.data.repository.TaskRepository
import xyz.ankitgrai.taskplanner.ui.components.TaskCard
import xyz.ankitgrai.taskplanner.ui.screen.taskdetail.TaskDetailScreen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

data class TaskListScreen(
    val categoryId: String,
    val categoryName: String,
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val taskRepository = koinInject<TaskRepository>()
        val scope = rememberCoroutineScope()

        val tasks by taskRepository.getTasksByCategory(categoryId).collectAsState(initial = emptyList())

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(categoryName) },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navigator.push(TaskDetailScreen(null, preselectedCategoryId = categoryId)) },
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
                if (tasks.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No tasks in this category",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        )
                    }
                } else {
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp),
                    ) {
                        items(tasks, key = { it.id }) { task ->
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
