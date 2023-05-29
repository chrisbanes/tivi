// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.shows

import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Provides

interface ShowsBinds {
    @ApplicationScope
    @Provides
    fun bindTraktShowDataSource(bind: TraktShowDataSourceImpl): TraktShowDataSource = bind

    @ApplicationScope
    @Provides
    fun bindTmdbShowDataSource(bind: TmdbShowDataSourceImpl): TmdbShowDataSource = bind
}

typealias TmdbShowDataSource = ShowDataSource
typealias TraktShowDataSource = ShowDataSource
