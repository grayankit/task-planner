package xyz.ankitgrai.kaizen.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import xyz.ankitgrai.kaizen.data.sync.SyncEnqueuer
import xyz.ankitgrai.kaizen.db.TaskPlannerDatabase
import xyz.ankitgrai.kaizen.shared.model.CategoryDto
import com.benasher44.uuid.uuid4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class CategoryRepository(
    private val database: TaskPlannerDatabase,
    private val syncEnqueuer: SyncEnqueuer,
) {
    fun getAllCategories(): Flow<List<CategoryDto>> {
        return database.categoryQueries.getAllCategories()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { entities -> entities.map { it.toDto() } }
    }

//    fun getCategoryById(id: String): CategoryDto? {
//        return database.categoryQueries.getCategoryById(id).executeAsOneOrNull()?.toDto()
//    }

    fun getDefaultCategory(): CategoryDto? {
        return database.categoryQueries.getDefaultCategory().executeAsOneOrNull()?.toDto()
    }

    fun createCategory(name: String, color: String?): CategoryDto {
        val id = uuid4().toString()
        val now = Clock.System.now().toString()

        val category = CategoryDto(
            id = id,
            userId = "",
            name = name,
            color = color,
            isDefault = false,
            createdAt = now,
            updatedAt = now,
        )

        database.categoryQueries.insertCategory(
            id = category.id,
            user_id = category.userId,
            name = category.name,
            color = category.color,
            is_default = 0,
            created_at = category.createdAt,
            updated_at = category.updatedAt,
        )

        syncEnqueuer.enqueueCreate("category", id, category)
        return category
    }

    fun updateCategory(id: String, name: String, color: String?): CategoryDto? {
        val now = Clock.System.now().toString()
        val existing = database.categoryQueries.getCategoryById(id).executeAsOneOrNull() ?: return null

        database.categoryQueries.updateCategory(
            name = name,
            color = color,
            updated_at = now,
            id = id,
        )

        val updated = existing.toDto().copy(name = name, color = color, updatedAt = now)
        syncEnqueuer.enqueueUpdate("category", id, updated)
        return updated
    }

    fun deleteCategory(id: String) {
        database.categoryQueries.deleteCategory(id)
        syncEnqueuer.enqueueDelete("category", id)
    }

    fun replaceAllCategories(categories: List<CategoryDto>) {
        database.categoryQueries.deleteAllCategories()
        categories.forEach { cat ->
            database.categoryQueries.insertCategory(
                id = cat.id,
                user_id = cat.userId,
                name = cat.name,
                color = cat.color,
                is_default = if (cat.isDefault) 1L else 0L,
                created_at = cat.createdAt,
                updated_at = cat.updatedAt,
            )
        }
    }

    fun upsertCategory(category: CategoryDto) {
        database.categoryQueries.insertCategory(
            id = category.id,
            user_id = category.userId,
            name = category.name,
            color = category.color,
            is_default = if (category.isDefault) 1L else 0L,
            created_at = category.createdAt,
            updated_at = category.updatedAt,
        )
    }
}

private fun xyz.ankitgrai.kaizen.db.CategoryEntity.toDto() = CategoryDto(
    id = id,
    userId = user_id,
    name = name,
    color = color,
    isDefault = is_default != 0L,
    createdAt = created_at,
    updatedAt = updated_at,
)
