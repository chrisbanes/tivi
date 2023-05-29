// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data

import android.app.Application
import me.tatarka.inject.annotations.Provides

interface AndroidSqlDelightDatabaseComponent : SqlDelightDatabaseComponent {
    @Provides
    fun provideDriverFactory(
        application: Application,
    ): DriverFactory = DriverFactory(
        context = application,
        databaseFilename = "shows.db",
    )
}
