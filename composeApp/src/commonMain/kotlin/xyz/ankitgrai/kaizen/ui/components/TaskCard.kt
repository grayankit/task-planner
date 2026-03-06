package xyz.ankitgrai.kaizen.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import xyz.ankitgrai.kaizen.shared.model.Priority
import xyz.ankitgrai.kaizen.shared.model.TaskDto
import xyz.ankitgrai.kaizen.ui.theme.priorityColor

@Composable
fun TaskCard(
    task: TaskDto,
    onToggleComplete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Priority indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(priorityColor(task.priority)),
            )

            Spacer(Modifier.width(12.dp))

            // Checkbox
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggleComplete() },
            )

            Spacer(Modifier.width(8.dp))

            // Task content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    color = if (task.isCompleted) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                val description = task.description
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Priority badge
                    val priority = Priority.fromValue(task.priority)
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = priority.label,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = priorityColor(task.priority).copy(alpha = 0.15f),
                            labelColor = priorityColor(task.priority),
                        ),
                        modifier = Modifier.height(24.dp),
                    )

                    // Due date
                    val dueDate = task.dueDate
                    if (dueDate != null) {
                        Text(
                            text = dueDate,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }

                    // Due time
                    val dueTime = task.dueTime
                    if (dueTime != null) {
                        Text(
                            text = dueTime,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }
                }
            }
        }
    }
}
