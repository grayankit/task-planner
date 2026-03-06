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
import androidx.compose.ui.unit.dp

@Composable
fun CategoryItem(
    name: String,
    color: String?,
    taskCount: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Color dot
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(
                    color?.let { parseColor(it) } ?: MaterialTheme.colorScheme.primary,
                ),
        )

        Spacer(Modifier.width(12.dp))

        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )

        if (taskCount > 0) {
            Text(
                text = taskCount.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
        }
    }
}

fun parseColor(hex: String): Color {
    return try {
        val cleaned = hex.removePrefix("#")
        val colorLong = cleaned.toLong(16)
        Color(
            red = ((colorLong shr 16) and 0xFF).toInt(),
            green = ((colorLong shr 8) and 0xFF).toInt(),
            blue = (colorLong and 0xFF).toInt(),
        )
    } catch (e: Exception) {
        Color.Gray
    }
}
