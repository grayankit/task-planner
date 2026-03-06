package xyz.ankitgrai.kaizen.ui.screen.tasklist

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import xyz.ankitgrai.kaizen.data.repository.TaskRepository
import xyz.ankitgrai.kaizen.ui.components.SectionedTaskList
import xyz.ankitgrai.kaizen.ui.screen.taskdetail.TaskDetailScreen
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
                        IconButton(onClick = { navigator.pop() }

                        ) {
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
                SectionedTaskList(
                    tasks = tasks,
                    emptyMessage = "No tasks in this category",
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
