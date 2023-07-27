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
                WorkaroundNativeSqliteDriver(
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
private class WorkaroundNativeSqliteDriver(
    schema: SqlSchema<QueryResult.Value<Unit>>,
    name: String,
    maxReaderConnections: Int = 1,
) : SqlDriver by NativeSqliteDriver(schema, name, maxReaderConnections) {
    // We can't use a mutable collection in a HashMap, as the value's hash
    // will change as the collection changes, which then makes HashMap happy.
    // In K/N this causes an IllegalStateException (but not on JVM)
    private val listeners = mutableMapOf<String, Set<Query.Listener>>()

    override fun addListener(vararg queryKeys: String, listener: Query.Listener) {
        queryKeys.forEach { key ->
            listeners[key] = (listeners[key] ?: emptySet()) + listener
        }
    }

    override fun removeListener(vararg queryKeys: String, listener: Query.Listener) {
        queryKeys.forEach { key ->
            listeners[key] = (listeners[key] ?: emptySet()) - listener
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
