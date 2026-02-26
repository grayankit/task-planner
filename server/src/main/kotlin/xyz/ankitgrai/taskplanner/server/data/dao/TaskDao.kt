package xyz.ankitgrai.taskplanner.server.data.dao

import xyz.ankitgrai.taskplanner.server.data.tables.Tasks
import xyz.ankitgrai.taskplanner.shared.model.TaskDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

class TaskDao {

    private fun resultRowToTask(row: ResultRow) = TaskDto(
        id = row[Tasks.id],
        userId = row[Tasks.userId],
        categoryId = row[Tasks.categoryId],
        title = row[Tasks.title],
        description = row[Tasks.description],
        priority = row[Tasks.priority],
        dueDate = row[Tasks.dueDate]?.toString(),
        dueTime = row[Tasks.dueTime]?.toString(),
        isCompleted = row[Tasks.isCompleted],
        completedAt = row[Tasks.completedAt]?.toString(),
        createdAt = row[Tasks.createdAt].toString(),
        updatedAt = row[Tasks.updatedAt].toString(),
    )

    suspend fun getAllByUser(userId: String): List<TaskDto> = dbQuery {
        Tasks.selectAll().where { Tasks.userId eq userId }
            .orderBy(Tasks.priority, SortOrder.ASC)
            .orderBy(Tasks.dueDate, SortOrder.ASC_NULLS_LAST)
            .map(::resultRowToTask)
    }

    suspend fun getByCategory(userId: String, categoryId: String): List<TaskDto> = dbQuery {
        Tasks.selectAll().where { (Tasks.userId eq userId) and (Tasks.categoryId eq categoryId) }
            .orderBy(Tasks.priority, SortOrder.ASC)
            .orderBy(Tasks.dueDate, SortOrder.ASC_NULLS_LAST)
            .map(::resultRowToTask)
    }

    suspend fun getById(id: String, userId: String): TaskDto? = dbQuery {
        Tasks.selectAll().where { (Tasks.id eq id) and (Tasks.userId eq userId) }
            .map(::resultRowToTask)
            .singleOrNull()
    }

    suspend fun create(
        userId: String,
        categoryId: String?,
        title: String,
        description: String?,
        priority: Int,
        dueDate: String?,
        dueTime: String?,
    ): TaskDto = dbQuery {
        val id = UUID.randomUUID().toString()
        val now = LocalDateTime.now()
        Tasks.insert {
            it[Tasks.id] = id
            it[Tasks.userId] = userId
            it[Tasks.categoryId] = categoryId
            it[Tasks.title] = title
            it[Tasks.description] = description
            it[Tasks.priority] = priority
            it[Tasks.dueDate] = dueDate?.let { d -> LocalDate.parse(d) }
            it[Tasks.dueTime] = dueTime?.let { t -> LocalTime.parse(t) }
            it[Tasks.isCompleted] = false
            it[Tasks.createdAt] = now
            it[Tasks.updatedAt] = now
        }
        TaskDto(
            id = id,
            userId = userId,
            categoryId = categoryId,
            title = title,
            description = description,
            priority = priority,
            dueDate = dueDate,
            dueTime = dueTime,
            isCompleted = false,
            createdAt = now.toString(),
            updatedAt = now.toString(),
        )
    }

    suspend fun createWithId(
        id: String,
        userId: String,
        categoryId: String?,
        title: String,
        description: String?,
        priority: Int,
        dueDate: String?,
        dueTime: String?,
        isCompleted: Boolean = false,
        completedAt: LocalDateTime? = null,
        createdAt: LocalDateTime = LocalDateTime.now(),
        updatedAt: LocalDateTime = LocalDateTime.now(),
    ): TaskDto = dbQuery {
        Tasks.insert {
            it[Tasks.id] = id
            it[Tasks.userId] = userId
            it[Tasks.categoryId] = categoryId
            it[Tasks.title] = title
            it[Tasks.description] = description
            it[Tasks.priority] = priority
            it[Tasks.dueDate] = dueDate?.let { d -> LocalDate.parse(d) }
            it[Tasks.dueTime] = dueTime?.let { t -> LocalTime.parse(t) }
            it[Tasks.isCompleted] = isCompleted
            it[Tasks.completedAt] = completedAt
            it[Tasks.createdAt] = createdAt
            it[Tasks.updatedAt] = updatedAt
        }
        TaskDto(
            id = id,
            userId = userId,
            categoryId = categoryId,
            title = title,
            description = description,
            priority = priority,
            dueDate = dueDate,
            dueTime = dueTime,
            isCompleted = isCompleted,
            completedAt = completedAt?.toString(),
            createdAt = createdAt.toString(),
            updatedAt = updatedAt.toString(),
        )
    }

    suspend fun update(
        id: String,
        userId: String,
        categoryId: String?,
        title: String?,
        description: String?,
        priority: Int?,
        dueDate: String?,
        dueTime: String?,
        isCompleted: Boolean?,
    ): TaskDto? = dbQuery {
        val now = LocalDateTime.now()
        val updated = Tasks.update({ (Tasks.id eq id) and (Tasks.userId eq userId) }) {
            if (categoryId != null) it[Tasks.categoryId] = categoryId
            if (title != null) it[Tasks.title] = title
            if (description != null) it[Tasks.description] = description
            if (priority != null) it[Tasks.priority] = priority
            if (dueDate != null) it[Tasks.dueDate] = LocalDate.parse(dueDate)
            if (dueTime != null) it[Tasks.dueTime] = LocalTime.parse(dueTime)
            if (isCompleted != null) {
                it[Tasks.isCompleted] = isCompleted
                it[Tasks.completedAt] = if (isCompleted) now else null
            }
            it[Tasks.updatedAt] = now
        }
        if (updated > 0) getById(id, userId) else null
    }

    suspend fun delete(id: String, userId: String): Boolean = dbQuery {
        Tasks.deleteWhere { (Tasks.id eq id) and (Tasks.userId eq userId) } > 0
    }

    suspend fun getModifiedSince(userId: String, since: LocalDateTime): List<TaskDto> = dbQuery {
        Tasks.selectAll().where {
            (Tasks.userId eq userId) and (Tasks.updatedAt greaterEq since)
        }.map(::resultRowToTask)
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction { block() }
}
