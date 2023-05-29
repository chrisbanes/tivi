// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.search

import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Provides

interface SearchBinds {
    val TmdbSearchDataSource.bind: SearchDataSource
        @ApplicationScope @Provides
        get() = this
}
