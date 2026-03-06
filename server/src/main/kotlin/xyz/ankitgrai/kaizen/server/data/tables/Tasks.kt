package xyz.ankitgrai.kaizen.server.data.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.javatime.time
import java.time.LocalDateTime

object Tasks : Table("tasks") {
    val id = varchar("id", 36)
    val userId = varchar("user_id", 36).references(Users.id)
    val categoryId = varchar("category_id", 36).references(Categories.id).nullable()
    val title = varchar("title", 255)
    val description = text("description").nullable()
    val priority = integer("priority").default(3)
    val dueDate = date("due_date").nullable()
    val dueTime = time("due_time").nullable()
    val isCompleted = bool("is_completed").default(false)
    val completedAt = datetime("completed_at").nullable()
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)
}
