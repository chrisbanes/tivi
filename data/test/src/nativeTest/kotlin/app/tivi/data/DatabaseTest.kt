package app.tivi.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.inMemoryDriver

internal actual fun createTestSqlDriver(name: String): SqlDriver {
  return inMemoryDriver(Database.Schema, name).also { driver ->
    driver.execute(null, "PRAGMA foreign_keys=ON", 0)
  }
}
