package com.taskplanner.ui.screen.taskdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.taskplanner.data.repository.CategoryRepository
import com.taskplanner.data.repository.TaskRepository
import com.taskplanner.shared.model.CategoryDto
import com.taskplanner.shared.model.Priority
import com.taskplanner.ui.components.PrioritySelector
import kotlinx.coroutines.launch
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
        var dueDate by remember { mutableStateOf("") }
        var dueTime by remember { mutableStateOf("") }
        var selectedCategoryId by remember { mutableStateOf(preselectedCategoryId) }
        var isInitialized by remember { mutableStateOf(false) }

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

                // Due date (text input — ISO format)
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due date (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )

                Spacer(Modifier.height(12.dp))

                // Due time (text input — ISO format)
                OutlinedTextField(
                    value = dueTime,
                    onValueChange = { dueTime = it },
                    label = { Text("Due time (HH:MM)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )

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
