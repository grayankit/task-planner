package xyz.ankitgrai.kaizen.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.GlanceAppWidgetManager
import kotlinx.coroutines.launch

class WidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var isReconfigure = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if this is a reconfigure launch from Settings
        isReconfigure = intent?.getBooleanExtra(EXTRA_RECONFIGURE, false) ?: false

        if (isReconfigure) {
            appWidgetId = intent?.getIntExtra(
                EXTRA_RECONFIGURE_WIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID,
            ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        } else {
            // Standard launcher configuration flow
            setResult(Activity.RESULT_CANCELED)
            appWidgetId = intent?.extras?.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID,
            ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val widgetPrefs = WidgetPreferences(this)
        val dataProvider = WidgetDataProvider(this)

        setContent {
            WidgetConfigScreen(
                widgetPrefs = widgetPrefs,
                dataProvider = dataProvider,
                appWidgetId = appWidgetId,
                onSave = { saveAndFinish() },
                onCancel = { finish() },
            )
        }
    }

    private fun saveAndFinish() {
        val scope = kotlinx.coroutines.MainScope()
        scope.launch {
            // Update the widget
            val manager = GlanceAppWidgetManager(this@WidgetConfigActivity)
            val glanceId = manager.getGlanceIdBy(appWidgetId)
            TaskPlannerWidget().update(this@WidgetConfigActivity, glanceId)

            if (!isReconfigure) {
                // Return success for launcher configuration flow
                val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                setResult(Activity.RESULT_OK, resultValue)
            }
            finish()
        }
    }

    companion object {
        const val EXTRA_RECONFIGURE = "extra_reconfigure"
        const val EXTRA_RECONFIGURE_WIDGET_ID = "extra_reconfigure_widget_id"

        fun reconfigureIntent(context: android.content.Context, appWidgetId: Int): Intent {
            return Intent(context, WidgetConfigActivity::class.java).apply {
                putExtra(EXTRA_RECONFIGURE, true)
                putExtra(EXTRA_RECONFIGURE_WIDGET_ID, appWidgetId)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WidgetConfigScreen(
    widgetPrefs: WidgetPreferences,
    dataProvider: WidgetDataProvider,
    appWidgetId: Int,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    val categories = remember { dataProvider.getCategories().filter { !it.isDefault } }
    var selectedListType by remember { mutableStateOf(widgetPrefs.getListType(appWidgetId)) }
    var opacity by remember { mutableStateOf(widgetPrefs.getOpacity(appWidgetId)) }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF90CAF9),
            surface = Color(0xFF1E1E1E),
            background = Color(0xFF121212),
            onSurface = Color(0xFFE0E0E0),
            onBackground = Color(0xFFE0E0E0),
        ),
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Configure Widget",
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF1E1E1E),
                        titleContentColor = Color(0xFFE0E0E0),
                    ),
                )
            },
            bottomBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E1E1E))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            widgetPrefs.setListType(appWidgetId, selectedListType)
                            widgetPrefs.setOpacity(appWidgetId, opacity)
                            // Store category name for display
                            if (selectedListType != WidgetPreferences.LIST_TYPE_MY_DAY) {
                                val cat = categories.find { it.id == selectedListType }
                                widgetPrefs.setCategoryName(appWidgetId, cat?.name ?: "Tasks")
                            } else {
                                widgetPrefs.setCategoryName(appWidgetId, "My Day")
                            }
                            onSave()
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Save")
                    }
                }
            },
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF121212))
                    .padding(padding)
                    .padding(horizontal = 16.dp),
            ) {
                // --- List Selection Section ---
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Select List",
                        color = Color(0xFF90CAF9),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // My Day option
                item {
                    ListOptionItem(
                        title = "My Day",
                        subtitle = "Tasks due today",
                        isSelected = selectedListType == WidgetPreferences.LIST_TYPE_MY_DAY,
                        indicatorColor = Color(0xFF1565C0),
                        onClick = { selectedListType = WidgetPreferences.LIST_TYPE_MY_DAY },
                    )
                }

                // Category options (excluding General/default)
                items(categories) { category ->
                    ListOptionItem(
                        title = category.name,
                        subtitle = null,
                        isSelected = selectedListType == category.id,
                        indicatorColor = category.color?.let { parseHexColor(it) } ?: Color(0xFF757575),
                        onClick = { selectedListType = category.id },
                    )
                }

                // --- Opacity Section ---
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Background Opacity",
                        color = Color(0xFF90CAF9),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Adjust transparency to see your wallpaper",
                        color = Color(0xFF9E9E9E),
                        fontSize = 12.sp,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    OpacitySelector(
                        opacity = opacity,
                        onOpacityChange = { opacity = it },
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun ListOptionItem(
    title: String,
    subtitle: String?,
    isSelected: Boolean,
    indicatorColor: Color,
    onClick: () -> Unit,
) {
    val borderColor = if (isSelected) Color(0xFF90CAF9) else Color(0xFF424242)
    val bgColor = if (isSelected) Color(0xFF1A237E).copy(alpha = 0.3f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(indicatorColor),
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color(0xFFE0E0E0),
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = Color(0xFF9E9E9E),
                    fontSize = 12.sp,
                )
            }
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF90CAF9)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "\u2713",
                    color = Color(0xFF121212),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun OpacitySelector(
    opacity: Float,
    onOpacityChange: (Float) -> Unit,
) {
    Column {
        // Preview box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    // Checkerboard-ish background to show transparency
                    Color(0xFF424242),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1E1E1E).copy(alpha = opacity)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Preview — ${(opacity * 100).toInt()}%",
                    color = Color(0xFFE0E0E0),
                    fontSize = 14.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Slider
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "0%",
                color = Color(0xFF9E9E9E),
                fontSize = 12.sp,
            )
            Slider(
                value = opacity,
                onValueChange = onOpacityChange,
                valueRange = 0f..1f,
                steps = 19,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
            )
            Text(
                text = "100%",
                color = Color(0xFF9E9E9E),
                fontSize = 12.sp,
            )
        }
    }
}

private fun parseHexColor(hex: String): Color {
    return try {
        val colorStr = hex.removePrefix("#")
        val colorLong = colorStr.toLong(16)
        when (colorStr.length) {
            6 -> Color(0xFF000000 or colorLong)
            8 -> Color(colorLong)
            else -> Color(0xFF757575)
        }
    } catch (_: Exception) {
        Color(0xFF757575)
    }
}
