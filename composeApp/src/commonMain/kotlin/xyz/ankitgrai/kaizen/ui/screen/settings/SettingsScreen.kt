package xyz.ankitgrai.kaizen.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import xyz.ankitgrai.kaizen.data.update.UpdateChecker
import xyz.ankitgrai.kaizen.ui.theme.ColorTheme
import xyz.ankitgrai.kaizen.ui.theme.ThemeManager
import xyz.ankitgrai.kaizen.ui.theme.ThemeMode
import xyz.ankitgrai.kaizen.util.getAppVersion
import xyz.ankitgrai.kaizen.util.isAndroid
import xyz.ankitgrai.kaizen.util.WidgetSettingsContent

class SettingsScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val themeManager = koinInject<ThemeManager>()
        val updateChecker = koinInject<UpdateChecker>()
        val scope = rememberCoroutineScope()

        val currentThemeMode by themeManager.themeMode.collectAsState()
        val currentColorTheme by themeManager.colorTheme.collectAsState()

        var isCheckingUpdate by remember { mutableStateOf(false) }
        var updateDialogMessage by remember { mutableStateOf<String?>(null) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Settings") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                )
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
            ) {
                Spacer(Modifier.height(8.dp))

                // --- Theme Mode Section ---
                Text(
                    text = "Appearance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )

                Spacer(Modifier.height(8.dp))

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    ThemeMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = currentThemeMode == mode,
                            onClick = { themeManager.setThemeMode(mode) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = ThemeMode.entries.size,
                            ),
                        ) {
                            Text(mode.displayName)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // --- Color Theme Section ---
                Text(
                    text = "Color Theme",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )

                Spacer(Modifier.height(12.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(ColorTheme.entries) { theme ->
                        ColorThemeItem(
                            theme = theme,
                            isSelected = currentColorTheme == theme,
                            onClick = { themeManager.setColorTheme(theme) },
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                HorizontalDivider()

                Spacer(Modifier.height(16.dp))

                // --- Updates Section (Android only) ---
                if (isAndroid()) {
                    Text(
                        text = "Updates",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                isCheckingUpdate = true
                                val result = updateChecker.checkForUpdate(getAppVersion())
                                isCheckingUpdate = false
                                updateDialogMessage = if (result == null) {
                                    "Unable to check for updates. Please check your internet connection."
                                } else if (result.isUpdateAvailable) {
                                    "Version ${result.latestVersion} is available!\n\nRelease notes:\n${result.releaseNotes.take(500)}"
                                } else {
                                    "You're on the latest version (${getAppVersion()})."
                                }
                            }
                        },
                        enabled = !isCheckingUpdate,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (isCheckingUpdate) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Checking...")
                        } else {
                            Text("Check for Updates")
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    HorizontalDivider()

                    Spacer(Modifier.height(16.dp))
                }

                // --- Widgets Section (Android only) ---
                if (isAndroid()) {
                    WidgetSettingsContent()
                }

                // --- About Section ---
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(Modifier.height(12.dp))

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
                            text = "Kaizen",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Version ${getAppVersion()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "A cross-platform task management app",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }

        // Update dialog
        if (updateDialogMessage != null) {
            AlertDialog(
                onDismissRequest = { updateDialogMessage = null },
                title = { Text("Update Check") },
                text = { Text(updateDialogMessage ?: "") },
                confirmButton = {
                    TextButton(onClick = { updateDialogMessage = null }) {
                        Text("OK")
                    }
                },
            )
        }
    }
}

@Composable
private fun ColorThemeItem(
    theme: ColorTheme,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(theme.previewColor)
                .then(
                    if (isSelected) {
                        Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                    } else {
                        Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
                    }
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = theme.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            },
        )
    }
}
