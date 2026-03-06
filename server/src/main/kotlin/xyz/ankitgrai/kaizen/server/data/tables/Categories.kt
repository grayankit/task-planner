package xyz.ankitgrai.kaizen.server.data.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object Categories : Table("categories") {
    val id = varchar("id", 36)
    val userId = varchar("user_id", 36).references(Users.id)
    val name = varchar("name", 100)
    val color = varchar("color", 7).nullable()
    val isDefault = bool("is_default").default(false)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)
}
