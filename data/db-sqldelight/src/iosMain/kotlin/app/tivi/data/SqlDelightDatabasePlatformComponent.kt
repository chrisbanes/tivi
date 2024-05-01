// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import app.cash.sqldelight.driver.native.wrapConnection
import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Provides

actual interface SqlDelightDatabasePlatformComponent {
  @Provides
  @ApplicationScope
  fun provideDriverFactory(): SqlDriver = NativeSqliteDriver(
    schema = Database.Schema,
    name = "tivi.db",
    maxReaderConnections = 4,
  ).apply {
    execute(null, "PRAGMA foreign_keys=ON", 0)
  }
}

fun inMemoryDriver(
  schema: SqlSchema<QueryResult.Value<Unit>>,
  name: String,
): NativeSqliteDriver = NativeSqliteDriver(
  co.touchlab.sqliter.DatabaseConfiguration(
    name = name,
    inMemory = true,
    version = if (schema.version > Int.MAX_VALUE) {
      error("Schema version is larger than Int.MAX_VALUE: ${schema.version}.")
    } else {
      schema.version.toInt()
    },
    create = { connection ->
      wrapConnection(connection) { schema.create(it) }
    },
    upgrade = { connection, oldVersion, newVersion ->
      wrapConnection(connection) { schema.migrate(it, oldVersion.toLong(), newVersion.toLong()) }
    },
  ),
)
