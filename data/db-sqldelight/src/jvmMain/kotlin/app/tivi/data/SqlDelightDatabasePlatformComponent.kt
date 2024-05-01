// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.tivi.inject.ApplicationScope
import java.io.File
import me.tatarka.inject.annotations.Provides

actual interface SqlDelightDatabasePlatformComponent {
  @Provides
  @ApplicationScope
  fun provideDriverFactory(): SqlDriver = JdbcSqliteDriver(
    url = "jdbc:sqlite:${databaseFile.absolutePath}",
  ).also { db ->
    Database.Schema.create(db)
    db.execute(null, "PRAGMA foreign_keys=ON", 0)
  }
}

private val databaseFile: File
  get() = File(appDir.also { if (!it.exists()) it.mkdirs() }, "tivi.db")

private val appDir: File
  get() {
    val os = System.getProperty("os.name").lowercase()
    return when {
      os.contains("win") -> {
        File(System.getenv("AppData"), "tivi/db")
      }

      os.contains("nix") || os.contains("nux") || os.contains("aix") -> {
        File(System.getProperty("user.home"), ".tivi")
      }

      os.contains("mac") -> {
        File(System.getProperty("user.home"), "Library/Application Support/tivi")
      }

      else -> error("Unsupported operating system")
    }
  }
