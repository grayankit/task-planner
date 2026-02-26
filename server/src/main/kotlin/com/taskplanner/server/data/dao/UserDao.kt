package com.taskplanner.server.data.dao

import com.taskplanner.server.data.tables.Users
import com.taskplanner.shared.model.UserDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime
import java.util.UUID

class UserDao {

    private fun resultRowToUser(row: ResultRow) = UserDto(
        id = row[Users.id],
        username = row[Users.username],
        createdAt = row[Users.createdAt].toString(),
    )

    suspend fun findByUsername(username: String): UserDto? = dbQuery {
        Users.selectAll().where { Users.username eq username }
            .map(::resultRowToUser)
            .singleOrNull()
    }

    suspend fun findById(id: String): UserDto? = dbQuery {
        Users.selectAll().where { Users.id eq id }
            .map(::resultRowToUser)
            .singleOrNull()
    }

    suspend fun getPasswordHash(username: String): String? = dbQuery {
        Users.selectAll().where { Users.username eq username }
            .map { it[Users.passwordHash] }
            .singleOrNull()
    }

    suspend fun create(username: String, passwordHash: String): UserDto = dbQuery {
        val id = UUID.randomUUID().toString()
        Users.insert {
            it[Users.id] = id
            it[Users.username] = username
            it[Users.passwordHash] = passwordHash
            it[Users.createdAt] = LocalDateTime.now()
        }
        UserDto(id = id, username = username, createdAt = LocalDateTime.now().toString())
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction { block() }
}
