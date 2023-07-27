// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data

import app.cash.sqldelight.Query
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
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
            else -> {
                FixedNativeSqliteDriver(
                    schema = Database.Schema,
                    name = "tivi.db",
                    maxReaderConnections = 4,
                )
            }
        }.also { driver ->
            driver.execute(null, "PRAGMA foreign_keys=ON", 0)
        }
    }
}

/**
 * [NativeSqliteDriver] wrapper to try and workaround
 * https://github.com/cashapp/sqldelight/issues/4376
 */
private class FixedNativeSqliteDriver(
    schema: SqlSchema<QueryResult.Value<Unit>>,
    name: String,
    maxReaderConnections: Int = 1,
) : SqlDriver by NativeSqliteDriver(schema, name, maxReaderConnections) {
    private val listeners = mutableMapOf<String, MutableSet<Query.Listener>>()

    override fun addListener(vararg queryKeys: String, listener: Query.Listener) {
        queryKeys.forEach {
            listeners.getOrPut(it) { mutableSetOf() }.add(listener)
        }
    }

    override fun removeListener(vararg queryKeys: String, listener: Query.Listener) {
        queryKeys.forEach {
            listeners[it]?.remove(listener)
        }
    }

    @Suppress("UselessCallOnCollection")
    override fun notifyListeners(vararg queryKeys: String) {
        queryKeys.flatMap { listeners[it] ?: emptySet() }
            .asSequence()
            // This shouldn't be necessary, but adding an extra guard to avoid
            // https://github.com/cashapp/sqldelight/issues/4376
            .filterNotNull()
            .forEach(Query.Listener::queryResultsChanged)
    }
}
