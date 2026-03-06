package xyz.ankitgrai.kaizen.server.data.dao

import xyz.ankitgrai.kaizen.server.data.tables.DeletedEntities
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime
import java.util.UUID

class DeletedEntityDao {

    suspend fun record(userId: String, entityType: String, entityId: String) = dbQuery {
        DeletedEntities.insert {
            it[DeletedEntities.id] = UUID.randomUUID().toString()
            it[DeletedEntities.userId] = userId
            it[DeletedEntities.entityType] = entityType
            it[DeletedEntities.entityId] = entityId
            it[DeletedEntities.deletedAt] = LocalDateTime.now()
        }
    }

    suspend fun getDeletedSince(userId: String, entityType: String, since: LocalDateTime): List<String> = dbQuery {
        DeletedEntities.selectAll().where {
            (DeletedEntities.userId eq userId) and
                (DeletedEntities.entityType eq entityType) and
                (DeletedEntities.deletedAt greaterEq since)
        }.map { it[DeletedEntities.entityId] }
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction { block() }
}
