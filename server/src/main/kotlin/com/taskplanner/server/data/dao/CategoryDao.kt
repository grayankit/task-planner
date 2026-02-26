package com.taskplanner.server.data.dao

import com.taskplanner.server.data.tables.Categories
import com.taskplanner.shared.model.CategoryDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime
import java.util.UUID

class CategoryDao {

    private fun resultRowToCategory(row: ResultRow) = CategoryDto(
        id = row[Categories.id],
        userId = row[Categories.userId],
        name = row[Categories.name],
        color = row[Categories.color],
        isDefault = row[Categories.isDefault],
        createdAt = row[Categories.createdAt].toString(),
        updatedAt = row[Categories.updatedAt].toString(),
    )

    suspend fun getAllByUser(userId: String): List<CategoryDto> = dbQuery {
        Categories.selectAll().where { Categories.userId eq userId }
            .orderBy(Categories.createdAt, SortOrder.ASC)
            .map(::resultRowToCategory)
    }

    suspend fun getById(id: String, userId: String): CategoryDto? = dbQuery {
        Categories.selectAll().where { (Categories.id eq id) and (Categories.userId eq userId) }
            .map(::resultRowToCategory)
            .singleOrNull()
    }

    suspend fun create(userId: String, name: String, color: String?, isDefault: Boolean = false): CategoryDto = dbQuery {
        val id = UUID.randomUUID().toString()
        val now = LocalDateTime.now()
        Categories.insert {
            it[Categories.id] = id
            it[Categories.userId] = userId
            it[Categories.name] = name
            it[Categories.color] = color
            it[Categories.isDefault] = isDefault
            it[Categories.createdAt] = now
            it[Categories.updatedAt] = now
        }
        CategoryDto(
            id = id,
            userId = userId,
            name = name,
            color = color,
            isDefault = isDefault,
            createdAt = now.toString(),
            updatedAt = now.toString(),
        )
    }

    suspend fun createWithId(
        id: String,
        userId: String,
        name: String,
        color: String?,
        isDefault: Boolean = false,
        createdAt: LocalDateTime = LocalDateTime.now(),
        updatedAt: LocalDateTime = LocalDateTime.now(),
    ): CategoryDto = dbQuery {
        Categories.insert {
            it[Categories.id] = id
            it[Categories.userId] = userId
            it[Categories.name] = name
            it[Categories.color] = color
            it[Categories.isDefault] = isDefault
            it[Categories.createdAt] = createdAt
            it[Categories.updatedAt] = updatedAt
        }
        CategoryDto(
            id = id,
            userId = userId,
            name = name,
            color = color,
            isDefault = isDefault,
            createdAt = createdAt.toString(),
            updatedAt = updatedAt.toString(),
        )
    }

    suspend fun update(id: String, userId: String, name: String?, color: String?): CategoryDto? = dbQuery {
        val now = LocalDateTime.now()
        val updated = Categories.update({ (Categories.id eq id) and (Categories.userId eq userId) }) {
            if (name != null) it[Categories.name] = name
            if (color != null) it[Categories.color] = color
            it[Categories.updatedAt] = now
        }
        if (updated > 0) getById(id, userId) else null
    }

    suspend fun delete(id: String, userId: String): Boolean = dbQuery {
        Categories.deleteWhere { (Categories.id eq id) and (Categories.userId eq userId) and (Categories.isDefault eq false) } > 0
    }

    suspend fun getModifiedSince(userId: String, since: LocalDateTime): List<CategoryDto> = dbQuery {
        Categories.selectAll().where {
            (Categories.userId eq userId) and (Categories.updatedAt greaterEq since)
        }.map(::resultRowToCategory)
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction { block() }
}
