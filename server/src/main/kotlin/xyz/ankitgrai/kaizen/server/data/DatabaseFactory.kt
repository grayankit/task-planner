package xyz.ankitgrai.kaizen.server.data

import xyz.ankitgrai.kaizen.server.data.tables.Categories
import xyz.ankitgrai.kaizen.server.data.tables.DeletedEntities
import xyz.ankitgrai.kaizen.server.data.tables.Tasks
import xyz.ankitgrai.kaizen.server.data.tables.Users
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.URI

object DatabaseFactory {
    fun init() {
        val database = Database.connect(createHikariDataSource())
        transaction(database) {
            SchemaUtils.create(Users, Categories, Tasks, DeletedEntities)
        }
    }

    /**
     * Parses a Neon/Postgres connection string in libpq format:
     *   postgresql://user:password@host/dbname?sslmode=require
     * and converts it to JDBC format with separate username/password properties,
     * since the PostgreSQL JDBC driver does not support credentials embedded in the URL.
     */
    private fun createHikariDataSource(): HikariDataSource {
        val dbUrl = System.getenv("DATABASE_URL")
            ?: error("DATABASE_URL environment variable is required")

        // Parse the URI to extract components
        val cleanUrl = if (dbUrl.startsWith("jdbc:", ignoreCase = true)) {
            dbUrl.removePrefix("jdbc:").removePrefix("JDBC:")
        } else {
            dbUrl
        }

        val uri = URI(cleanUrl)
        val host = uri.host
        val port = if (uri.port > 0) uri.port else 5432
        val dbName = uri.path.removePrefix("/")
        val userInfo = uri.userInfo // "user:password"
        val query = uri.query // "sslmode=require&channel_binding=require"

        val username = userInfo?.substringBefore(":")
        val password = userInfo?.substringAfter(":")

        // Build clean JDBC URL without credentials
        val jdbcUrl = buildString {
            append("jdbc:postgresql://$host:$port/$dbName")
            if (!query.isNullOrBlank()) {
                append("?$query")
            }
        }

        val config = HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            this.username = username
            this.password = password
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 5
            minimumIdle = 1
            idleTimeout = 60_000
            connectionTimeout = 20_000
            maxLifetime = 300_000
            isAutoCommit = false
        }

        return HikariDataSource(config)
    }
}
