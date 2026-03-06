package xyz.ankitgrai.kaizen.util

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import xyz.ankitgrai.kaizen.widget.TaskPlannerWidgetReceiver
import xyz.ankitgrai.kaizen.widget.WidgetConfigActivity
import xyz.ankitgrai.kaizen.widget.WidgetPreferences

actual fun isAndroid(): Boolean = true

@Composable
actual fun WidgetSettingsContent() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var widgetIds by remember { mutableStateOf(intArrayOf()) }
    val widgetPrefs = remember { WidgetPreferences(context) }

    // Refresh widget IDs when the lifecycle resumes (e.g., returning from config activity)
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    LaunchedEffect(lifecycleState) {
        if (lifecycleState.isAtLeast(Lifecycle.State.RESUMED)) {
            val manager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, TaskPlannerWidgetReceiver::class.java)
            widgetIds = manager.getAppWidgetIds(componentName)
        }
    }

    Text(
        text = "Widgets",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
    )

    Spacer(Modifier.height(12.dp))

    if (widgetIds.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = "No widgets active",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Long-press your home screen and add a Kaizen widget to configure it here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
        }
    } else {
        widgetIds.forEachIndexed { index, widgetId ->
            val listType = widgetPrefs.getListType(widgetId)
            val categoryName = widgetPrefs.getCategoryName(widgetId)
            val opacity = widgetPrefs.getOpacity(widgetId)
            val displayName = if (listType == WidgetPreferences.LIST_TYPE_MY_DAY) {
                "My Day"
            } else {
                categoryName.ifEmpty { "Category" }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = WidgetConfigActivity.reconfigureIntent(context, widgetId)
                        context.startActivity(intent)
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (widgetIds.size > 1) "Widget ${index + 1}" else "Widget",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "List: $displayName  \u00B7  Opacity: ${(opacity * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configure widget",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            if (index < widgetIds.size - 1) {
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    Spacer(Modifier.height(16.dp))

    HorizontalDivider()

    Spacer(Modifier.height(16.dp))
}
