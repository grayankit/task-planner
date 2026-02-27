package xyz.ankitgrai.taskplanner.ui.screen.taskdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import xyz.ankitgrai.taskplanner.data.repository.CategoryRepository
import xyz.ankitgrai.taskplanner.data.repository.TaskRepository
import xyz.ankitgrai.taskplanner.shared.model.CategoryDto
import xyz.ankitgrai.taskplanner.shared.model.Priority
import xyz.ankitgrai.taskplanner.ui.components.PrioritySelector
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import org.koin.compose.koinInject

data class TaskDetailScreen(
    val taskId: String?,
    val preselectedCategoryId: String? = null,
) : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val taskRepository = koinInject<TaskRepository>()
        val categoryRepository = koinInject<CategoryRepository>()
        val scope = rememberCoroutineScope()

        val isEditing = taskId != null
        val existingTask by taskRepository.getTaskById(taskId ?: "").collectAsState(initial = null)
        val categories by categoryRepository.getAllCategories().collectAsState(initial = emptyList())

        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var priority by remember { mutableStateOf(Priority.MEDIUM.value) }
        val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()).toString() }
        var dueDate by remember { mutableStateOf(today) }
        var dueTime by remember { mutableStateOf("") }
        var selectedCategoryId by remember { mutableStateOf(preselectedCategoryId) }
        var isInitialized by remember { mutableStateOf(false) }
        var showDatePicker by remember { mutableStateOf(false) }
        var showTimePicker by remember { mutableStateOf(false) }

        // Default to General category when creating a new task with no preselected category
        LaunchedEffect(categories) {
            if (!isEditing && selectedCategoryId == null && categories.isNotEmpty()) {
                val defaultCategory = categoryRepository.getDefaultCategory()
                selectedCategoryId = defaultCategory?.id ?: categories.first().id
            }
        }

        // Initialize form with existing task data
        LaunchedEffect(existingTask) {
            if (isEditing && existingTask != null && !isInitialized) {
                val task = existingTask!!
                title = task.title
                description = task.description ?: ""
                priority = task.priority
                dueDate = task.dueDate ?: ""
                dueTime = task.dueTime ?: ""
                selectedCategoryId = task.categoryId
                isInitialized = true
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (isEditing) "Edit Task" else "New Task") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        if (isEditing && taskId != null) {
                            IconButton(onClick = {
                                scope.launch {
                                    taskRepository.deleteTask(taskId)
                                    navigator.pop()
                                }
                            }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    },
                )
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(Modifier.height(8.dp))

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )

                Spacer(Modifier.height(12.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )

                Spacer(Modifier.height(16.dp))

                // Priority
                PrioritySelector(
                    selectedPriority = priority,
                    onPrioritySelected = { priority = it },
                )

                Spacer(Modifier.height(16.dp))

                // Category
                CategoryDropdown(
                    categories = categories,
                    selectedCategoryId = selectedCategoryId,
                    onCategorySelected = { selectedCategoryId = it },
                )

                Spacer(Modifier.height(16.dp))

                // Due date picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = if (dueDate.isNotBlank()) formatDateDisplay(dueDate) else "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Due date") },
                        placeholder = { Text("Select date") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            Row {
                                if (dueDate.isNotBlank()) {
                                    IconButton(onClick = { dueDate = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear date", modifier = Modifier.size(20.dp))
                                    }
                                }
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(Icons.Default.DateRange, contentDescription = "Pick date")
                                }
                            }
                        },
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Due time picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = if (dueTime.isNotBlank()) formatTimeDisplay(dueTime) else "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Due time") },
                        placeholder = { Text("Select time") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            Row {
                                if (dueTime.isNotBlank()) {
                                    IconButton(onClick = { dueTime = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear time", modifier = Modifier.size(20.dp))
                                    }
                                }
                                IconButton(onClick = { showTimePicker = true }) {
                                    Icon(Icons.Default.DateRange, contentDescription = "Pick time")
                                }
                            }
                        },
                    )
                }

                // Date picker dialog
                if (showDatePicker) {
                    val initialMillis = if (dueDate.isNotBlank()) {
                        try {
                            LocalDate.parse(dueDate)
                                .atStartOfDayIn(TimeZone.UTC)
                                .toEpochMilliseconds()
                        } catch (_: Exception) { null }
                    } else null

                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = initialMillis
                            ?: Clock.System.now().toEpochMilliseconds(),
                    )

                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val instant = Instant.fromEpochMilliseconds(millis)
                                    val date = instant.toLocalDateTime(TimeZone.UTC).date
                                    dueDate = date.toString()
                                }
                                showDatePicker = false
                            }) { Text("OK") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                        },
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

                // Time picker dialog
                if (showTimePicker) {
                    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    val initialHour = if (dueTime.isNotBlank()) {
                        try { dueTime.substringBefore(":").toInt() } catch (_: Exception) { now.hour }
                    } else now.hour
                    val initialMinute = if (dueTime.isNotBlank()) {
                        try { dueTime.substringAfter(":").toInt() } catch (_: Exception) { now.minute }
                    } else now.minute

                    val timePickerState = rememberTimePickerState(
                        initialHour = initialHour,
                        initialMinute = initialMinute,
                    )

                    AlertDialog(
                        onDismissRequest = { showTimePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                dueTime = "%02d:%02d".format(timePickerState.hour, timePickerState.minute)
                                showTimePicker = false
                            }) { Text("OK") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                        },
                        title = { Text("Select time") },
                        text = { TimePicker(state = timePickerState) },
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Save button
                Button(
                    onClick = {
                        scope.launch {
                            if (title.isBlank()) return@launch

                            if (isEditing && taskId != null) {
                                taskRepository.updateTask(
                                    id = taskId,
                                    categoryId = selectedCategoryId,
                                    title = title,
                                    description = description.ifBlank { null },
                                    priority = priority,
                                    dueDate = dueDate.ifBlank { null },
                                    dueTime = dueTime.ifBlank { null },
                                )
                            } else {
                                taskRepository.createTask(
                                    categoryId = selectedCategoryId,
                                    title = title,
                                    description = description.ifBlank { null },
                                    priority = priority,
                                    dueDate = dueDate.ifBlank { null },
                                    dueTime = dueTime.ifBlank { null },
                                )
                            }
                            navigator.pop()
                        }
                    },
                    enabled = title.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(if (isEditing) "Update Task" else "Create Task")
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

private fun formatDateDisplay(isoDate: String): String {
    return try {
        val date = LocalDate.parse(isoDate)
        val months = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
        )
        "${months[date.monthNumber - 1]} ${date.dayOfMonth}, ${date.year}"
    } catch (_: Exception) {
        isoDate
    }
}

private fun formatTimeDisplay(isoTime: String): String {
    return try {
        val parts = isoTime.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()
        val amPm = if (hour < 12) "AM" else "PM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        "%d:%02d %s".format(displayHour, minute, amPm)
    } catch (_: Exception) {
        isoTime
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    categories: List<CategoryDto>,
    selectedCategoryId: String?,
    onCategorySelected: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedCategory = categories.find { it.id == selectedCategoryId }

    Column {
        Text(
            text = "Category",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
        Spacer(Modifier.height(4.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                value = selectedCategory?.name ?: "Select category",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(12.dp),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name) },
                        onClick = {
                            onCategorySelected(category.id)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}
