package app.tivi.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

internal actual fun createTestSqlDriver(name: String): SqlDriver {
  return JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { db ->
    Database.Schema.create(db)
    db.execute(null, "PRAGMA foreign_keys=ON", 0)
  }
}
