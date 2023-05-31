// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import app.cash.sqldelight.driver.native.inMemoryDriver
import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Provides

actual interface SqlDelightDatabasePlatformComponent {
    @Provides
    @ApplicationScope
    fun provideDriverFactory(configuration: DatabaseConfiguration): SqlDriver {
        return when {
            configuration.inMemory -> inMemoryDriver(Database.Schema)
            else -> NativeSqliteDriver(schema = Database.Schema, name = "tivi.db")
        }.also { driver ->
            driver.execute(null, "PRAGMA foreign_keys=ON", 0)
        }
    }
}
