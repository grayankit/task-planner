package xyz.ankitgrai.kaizen.ui.screen.categories

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import xyz.ankitgrai.kaizen.data.repository.CategoryRepository
import xyz.ankitgrai.kaizen.shared.model.CategoryDto
import xyz.ankitgrai.kaizen.ui.components.parseColor
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

class CategoriesScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val categoryRepository = koinInject<CategoryRepository>()
        val scope = rememberCoroutineScope()

        val categories by categoryRepository.getAllCategories().collectAsState(initial = emptyList())

        var showCreateDialog by remember { mutableStateOf(false) }
        var editingCategory by remember { mutableStateOf<CategoryDto?>(null) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Categories") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Category")
                }
            },
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                items(categories, key = { it.id }) { category ->
                    CategoryCard(
                        category = category,
                        onEdit = { editingCategory = category },
                        onDelete = {
                            scope.launch { categoryRepository.deleteCategory(category.id) }
                        },
                    )
                }
            }
        }

        // Create dialog
        if (showCreateDialog) {
            CategoryDialog(
                title = "New Category",
                initialName = "",
                initialColor = "#42A5F5",
                existingCategories = categories,
                excludeCategoryId = null,
                onDismiss = { showCreateDialog = false },
                onSave = { name, color ->
                    scope.launch {
                        categoryRepository.createCategory(name, color)
                        showCreateDialog = false
                    }
                },
            )
        }

        // Edit dialog
        if (editingCategory != null) {
            CategoryDialog(
                title = "Edit Category",
                initialName = editingCategory!!.name,
                initialColor = editingCategory!!.color ?: "#42A5F5",
                existingCategories = categories,
                excludeCategoryId = editingCategory!!.id,
                onDismiss = { editingCategory = null },
                onSave = { name, color ->
                    scope.launch {
                        categoryRepository.updateCategory(editingCategory!!.id, name, color)
                        editingCategory = null
                    }
                },
            )
        }
    }
}

@Composable
private fun CategoryCard(
    category: CategoryDto,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Color chip
            Surface(
                modifier = Modifier.size(32.dp),
                shape = RoundedCornerShape(8.dp),
                color = category.color?.let { parseColor(it) }
                    ?: MaterialTheme.colorScheme.primary,
            ) {}

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyLarge,
                )
                if (category.isDefault) {
                    Text(
                        text = "Default",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                }
            }

            if (!category.isDefault) {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(20.dp),
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryDialog(
    title: String,
    initialName: String,
    initialColor: String,
    existingCategories: List<CategoryDto>,
    excludeCategoryId: String?,
    onDismiss: () -> Unit,
    onSave: (name: String, color: String) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var color by remember { mutableStateOf(initialColor) }

    val isDuplicate = remember(name, existingCategories, excludeCategoryId) {
        name.isNotBlank() && existingCategories.any { cat ->
            cat.id != excludeCategoryId && cat.name.equals(name.trim(), ignoreCase = true)
        }
    }

    val presetColors = listOf(
        "#E53935", "#FB8C00", "#FDD835", "#43A047",
        "#42A5F5", "#7E57C2", "#EC407A", "#78909C",
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = isDuplicate,
                    supportingText = if (isDuplicate) {
                        { Text("A category with this name already exists") }
                    } else {
                        null
                    },
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Color",
                    style = MaterialTheme.typography.labelMedium,
                )

                Spacer(Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    presetColors.forEach { presetColor ->
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = parseColor(presetColor),
                            onClick = { color = presetColor },
                            border = if (color == presetColor) {
                                BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
                            } else {
                                null
                            },
                        ) {}
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name.trim(), color) },
                enabled = name.isNotBlank() && !isDuplicate,
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
