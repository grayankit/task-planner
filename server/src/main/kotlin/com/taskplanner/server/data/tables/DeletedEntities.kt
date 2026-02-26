package com.taskplanner.server.data.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object DeletedEntities : Table("deleted_entities") {
    val id = varchar("id", 36)
    val userId = varchar("user_id", 36).references(Users.id)
    val entityType = varchar("entity_type", 20) // "task" or "category"
    val entityId = varchar("entity_id", 36)
    val deletedAt = datetime("deleted_at").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)
}
