package xyz.ankitgrai.kaizen.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.ankitgrai.kaizen.shared.model.TaskDto

@Composable
fun SectionedTaskList(
    modifier: Modifier = Modifier,
    tasks: List<TaskDto>,
    emptyMessage: String,
    emptySubMessage: String? = null,
    onToggleComplete: (TaskDto) -> Unit,
    onTaskClick: (TaskDto) -> Unit,

) {
    val pendingTasks = tasks.filter { !it.isCompleted }
    val completedTasks = tasks.filter { it.isCompleted }

    if (tasks.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = emptyMessage,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
                if (emptySubMessage != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = emptySubMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    )
                }
            }
        }
    } else {
        var completedExpanded by remember { mutableStateOf(false) }

        LazyColumn(
            modifier = modifier,
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
                items(pendingTasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onToggleComplete = { onToggleComplete(task) },
                        onClick = { onTaskClick(task) },
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
                    items(completedTasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            onToggleComplete = { onToggleComplete(task) },
                            onClick = { onTaskClick(task) },
                        )
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
