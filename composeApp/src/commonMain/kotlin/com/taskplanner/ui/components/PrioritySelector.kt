package com.taskplanner.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.taskplanner.shared.model.Priority

@Composable
fun PrioritySelector(
    selectedPriority: Int,
    onPrioritySelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Priority",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
        Spacer(Modifier.height(4.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Priority.entries.forEach { priority ->
                val isSelected = priority.value == selectedPriority
                FilterChip(
                    selected = isSelected,
                    onClick = { onPrioritySelected(priority.value) },
                    label = { Text(priority.label, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = com.taskplanner.ui.theme.priorityColor(priority.value).copy(alpha = 0.2f),
                        selectedLabelColor = com.taskplanner.ui.theme.priorityColor(priority.value),
                    ),
                )
            }
        }
    }
}
