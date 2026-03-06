package xyz.ankitgrai.kaizen.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider as ColorProviderType
import xyz.ankitgrai.kaizen.MainActivity

@Composable
fun TaskWidgetContent(
    title: String,
    tasks: List<WidgetTask>,
    opacity: Float,
    appWidgetId: Int,
) {
    val backgroundColor = ColorProvider(
        day = Color.White.copy(alpha = opacity),
        night = Color(0xFF1E1E1E).copy(alpha = opacity),
    )
    val headerColor = ColorProvider(
        day = Color(0xFF1565C0),
        night = Color(0xFF90CAF9),
    )
    val textColor = ColorProvider(
        day = Color(0xFF212121),
        night = Color(0xFFE0E0E0),
    )
    val subtextColor = ColorProvider(
        day = Color(0xFF757575),
        night = Color(0xFF9E9E9E),
    )
    val dividerColor = ColorProvider(
        day = Color(0xFFE0E0E0),
        night = Color(0xFF424242),
    )

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(backgroundColor)
            .cornerRadius(16.dp),
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
        ) {
            // --- Header ---
            WidgetHeader(
                title = title,
                headerColor = headerColor,
                textColor = textColor,
                subtextColor = subtextColor,
            )

            // --- Divider ---
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(dividerColor),
            ) {}

            // --- Task list ---
            if (tasks.isEmpty()) {
                EmptyState(
                    textColor = subtextColor,
                    modifier = GlanceModifier.defaultWeight().fillMaxWidth(),
                )
            } else {
                TaskList(
                    tasks = tasks,
                    textColor = textColor,
                    subtextColor = subtextColor,
                    dividerColor = dividerColor,
                    appWidgetId = appWidgetId,
                    modifier = GlanceModifier.defaultWeight().fillMaxWidth(),
                )
            }

            // --- Bottom bar ---
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(dividerColor),
            ) {}
            WidgetBottomBar(
                subtextColor = subtextColor,
                appWidgetId = appWidgetId,
            )
        }
    }
}

@Composable
private fun WidgetHeader(
    title: String,
    headerColor: ColorProviderType,
    textColor: ColorProviderType,
    subtextColor: ColorProviderType,
) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Colored dot indicator
        Box(
            modifier = GlanceModifier
                .size(10.dp)
                .cornerRadius(5.dp)
                .background(headerColor),
        ) {}

        Spacer(modifier = GlanceModifier.width(10.dp))

        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = title,
                style = TextStyle(
                    color = textColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Text(
                text = java.time.LocalDate.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d"),
                ),
                style = TextStyle(
                    color = subtextColor,
                    fontSize = 12.sp,
                ),
            )
        }

        // Settings gear icon — opens config activity
        Text(
            text = "\u2699",
            modifier = GlanceModifier
                .padding(4.dp)
                .clickable(
                    actionStartActivity<WidgetConfigActivity>(),
                ),
            style = TextStyle(
                color = subtextColor,
                fontSize = 18.sp,
            ),
        )
    }
}

@Composable
private fun TaskList(
    tasks: List<WidgetTask>,
    textColor: ColorProviderType,
    subtextColor: ColorProviderType,
    dividerColor: ColorProviderType,
    appWidgetId: Int,
    modifier: GlanceModifier = GlanceModifier,
) {
    // Show incomplete tasks first, then completed
    val sortedTasks = tasks.sortedBy { it.isCompleted }

    LazyColumn(
        modifier = modifier,
    ) {
        items(sortedTasks, itemId = { it.id.hashCode().toLong() }) { task ->
            TaskRow(
                task = task,
                textColor = textColor,
                subtextColor = subtextColor,
                dividerColor = dividerColor,
                appWidgetId = appWidgetId,
            )
        }
    }
}

@Composable
private fun TaskRow(
    task: WidgetTask,
    textColor: ColorProviderType,
    subtextColor: ColorProviderType,
    dividerColor: ColorProviderType,
    appWidgetId: Int,
) {
    val priorityColor = when (task.priority) {
        1 -> ColorProvider(
            day = Color(0xFFE53935),
            night = Color(0xFFEF9A9A),
        )
        2 -> ColorProvider(
            day = Color(0xFFFB8C00),
            night = Color(0xFFFFB74D),
        )
        3 -> ColorProvider(
            day = Color(0xFFFDD835),
            night = Color(0xFFFFF176),
        )
        else -> ColorProvider(
            day = Color(0xFF42A5F5),
            night = Color(0xFF90CAF9),
        )
    }

    Column {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .clickable(
                    actionRunCallback<ToggleTaskAction>(
                        actionParametersOf(
                            TaskPlannerWidget.PARAM_TASK_ID to task.id,
                            TaskPlannerWidget.PARAM_COMPLETED to (!task.isCompleted).toString(),
                            TaskPlannerWidget.PARAM_WIDGET_ID to appWidgetId.toString(),
                        ),
                    ),
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Checkbox circle
            Box(
                modifier = GlanceModifier
                    .size(22.dp)
                    .cornerRadius(11.dp)
                    .background(
                        if (task.isCompleted) priorityColor
                        else ColorProvider(
                            day = Color(0xFFBDBDBD),
                            night = Color(0xFF616161),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (task.isCompleted) {
                    Text(
                        text = "\u2713",
                        style = TextStyle(
                            color = ColorProvider(
                                day = Color.White,
                                night = Color.White,
                            ),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }
            }

            Spacer(modifier = GlanceModifier.width(12.dp))

            // Priority indicator bar
            Box(
                modifier = GlanceModifier
                    .width(3.dp)
                    .height(20.dp)
                    .cornerRadius(2.dp)
                    .background(priorityColor),
            ) {}

            Spacer(modifier = GlanceModifier.width(8.dp))

            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = task.title,
                    maxLines = 2,
                    style = TextStyle(
                        color = if (task.isCompleted) subtextColor else textColor,
                        fontSize = 14.sp,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    ),
                )
                if (task.dueTime != null) {
                    Text(
                        text = task.dueTime,
                        style = TextStyle(
                            color = subtextColor,
                            fontSize = 11.sp,
                        ),
                    )
                }
            }
        }

        // Row divider
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(1.dp)
                .background(dividerColor),
        ) {}
    }
}

@Composable
private fun EmptyState(
    textColor: ColorProviderType,
    modifier: GlanceModifier = GlanceModifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "\u2600",
                style = TextStyle(fontSize = 32.sp),
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            Text(
                text = "No tasks",
                style = TextStyle(
                    color = textColor,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                ),
            )
            Text(
                text = "Tap + to add one",
                style = TextStyle(
                    color = textColor,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                ),
            )
        }
    }
}

@Composable
private fun WidgetBottomBar(
    subtextColor: ColorProviderType,
    appWidgetId: Int,
) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.End,
    ) {
        // Add task button — opens the main app
        Text(
            text = "\u002B",
            modifier = GlanceModifier
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clickable(actionStartActivity<MainActivity>()),
            style = TextStyle(
                color = subtextColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            ),
        )

        Spacer(modifier = GlanceModifier.width(16.dp))

        // Refresh button
        Text(
            text = "\u21BB",
            modifier = GlanceModifier
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clickable(
                    actionRunCallback<RefreshWidgetAction>(
                        actionParametersOf(
                            TaskPlannerWidget.PARAM_WIDGET_ID to appWidgetId.toString(),
                        ),
                    ),
                ),
            style = TextStyle(
                color = subtextColor,
                fontSize = 20.sp,
            ),
        )
    }
}
