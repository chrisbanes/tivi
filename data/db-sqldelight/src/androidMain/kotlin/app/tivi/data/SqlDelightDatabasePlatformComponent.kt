// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data

import android.app.Application
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Provides

actual interface SqlDelightDatabasePlatformComponent {
    @Provides
    @ApplicationScope
    fun provideDriverFactory(
        application: Application,
    ): SqlDriver = AndroidSqliteDriver(
        schema = Database.Schema,
        context = application,
        name = "shows.db",
        callback = object : AndroidSqliteDriver.Callback(Database.Schema) {
            override fun onConfigure(db: SupportSQLiteDatabase) {
                db.enableWriteAheadLogging()
                db.setForeignKeyConstraintsEnabled(true)
            }
        },
    )
}
