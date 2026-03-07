package xyz.ankitgrai.kaizen.ui.screen.alltasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import xyz.ankitgrai.kaizen.data.repository.TaskRepository
import xyz.ankitgrai.kaizen.shared.model.TaskDto
import xyz.ankitgrai.kaizen.ui.components.TaskCard
import xyz.ankitgrai.kaizen.ui.screen.taskdetail.TaskDetailScreen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

class AllTasksScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val taskRepository = koinInject<TaskRepository>()
        val scope = rememberCoroutineScope()

        val pageSize = 20L
        var tasks by remember { mutableStateOf<List<TaskDto>>(emptyList()) }
        var totalCount by remember { mutableStateOf(0L) }
        var currentOffset by remember { mutableStateOf(0L) }
        var isLoading by remember { mutableStateOf(false) }
        val hasMore by remember { derivedStateOf { currentOffset < totalCount } }

        val listState = rememberLazyListState()

        // Load initial page
        LaunchedEffect(Unit) {
            totalCount = taskRepository.getTaskCount()
            val page = taskRepository.getTasksPaginated(pageSize, 0L)
            tasks = page
            currentOffset = page.size.toLong()
        }

        // Detect when user scrolls near the bottom and load more
        val shouldLoadMore by remember {
            derivedStateOf {
                val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                val totalItems = listState.layoutInfo.totalItemsCount
                lastVisibleItem >= totalItems - 3 && !isLoading && hasMore
            }
        }

        LaunchedEffect(shouldLoadMore) {
            if (shouldLoadMore) {
                isLoading = true
                val page = taskRepository.getTasksPaginated(pageSize, currentOffset)
                tasks = tasks + page
                currentOffset += page.size.toLong()
                isLoading = false
            }
        }

        // Refresh tasks list after returning from detail screen
        LaunchedEffect(navigator.lastItem) {
            if (navigator.lastItem is AllTasksScreen) {
                totalCount = taskRepository.getTaskCount()
                val refreshed = taskRepository.getTasksPaginated(pageSize, 0L)
                // Reload all pages up to current offset to pick up changes
                val allLoaded = if (currentOffset > pageSize) {
                    taskRepository.getTasksPaginated(currentOffset, 0L)
                } else {
                    refreshed
                }
                tasks = allLoaded
                currentOffset = allLoaded.size.toLong()
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("All Tasks") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            if (tasks.isEmpty() && !isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No tasks yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Create a task to get started",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        )
                    }
                }
            } else {
                val pendingTasks = tasks.filter { !it.isCompleted }
                val completedTasks = tasks.filter { it.isCompleted }
                var completedExpanded by remember { mutableStateOf(false) }

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                ) {
                    // -- Pending section --
                    item(key = "header_pending") {
                        SectionHeader(
                            title = "Pending",
                            count = pendingTasks.size,
                        )
                    }

                    if (pendingTasks.isEmpty()) {
                        item(key = "empty_pending") {
                            Text(
                                text = "No pending tasks",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp, bottom = 8.dp),
                            )
                        }
                    } else {
                        items(pendingTasks, key = { "pending_${it.id}" }) { task ->
                            TaskCard(
                                task = task,
                                onToggleComplete = {
                                    scope.launch {
                                        taskRepository.toggleTaskComplete(task.id)
                                        // Refresh current loaded tasks
                                        totalCount = taskRepository.getTaskCount()
                                        tasks = taskRepository.getTasksPaginated(currentOffset, 0L)
                                    }
                                },
                                onClick = { navigator.push(TaskDetailScreen(task.id)) },
                            )
                        }
                    }

                    // -- Completed section (collapsible) --
                    if (completedTasks.isNotEmpty()) {
                        item(key = "header_completed") {
                            Spacer(Modifier.height(8.dp))
                            SectionHeader(
                                title = "Completed",
                                count = completedTasks.size,
                                collapsible = true,
                                expanded = completedExpanded,
                                onToggle = { completedExpanded = !completedExpanded },
                            )
                        }

                        if (completedExpanded) {
                            items(completedTasks, key = { "completed_${it.id}" }) { task ->
                                TaskCard(
                                    task = task,
                                    onToggleComplete = {
                                        scope.launch {
                                            taskRepository.toggleTaskComplete(task.id)
                                            totalCount = taskRepository.getTaskCount()
                                            tasks = taskRepository.getTasksPaginated(currentOffset, 0L)
                                        }
                                    },
                                    onClick = { navigator.push(TaskDetailScreen(task.id)) },
                                )
                            }
                        }
                    }

                    // -- Loading indicator --
                    if (isLoading) {
                        item(key = "loading") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
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
private fun SectionHeader(
    title: String,
    count: Int,
    collapsible: Boolean = false,
    expanded: Boolean = false,
    onToggle: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (collapsible && onToggle != null) {
                    Modifier.clickable(onClick = onToggle)
                } else {
                    Modifier
                }
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$title ($count)",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )

        if (collapsible) {
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = if (expanded) {
                    Icons.Default.KeyboardArrowUp
                } else {
                    Icons.Default.KeyboardArrowDown
                },
                contentDescription = if (expanded) "Collapse" else "Expand",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
        }
    }
}
