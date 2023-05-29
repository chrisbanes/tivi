// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.traktauth

import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Provides

interface RelatedShowsBinds {
    @ApplicationScope
    @Provides
    fun provideTmdbRelatedShowsDataSource(
        bind: TmdbRelatedShowsDataSourceImpl,
    ): TmdbRelatedShowsDataSource = bind

    @ApplicationScope
    @Provides
    fun provideTraktRelatedShowsDataSource(
        bind: TraktRelatedShowsDataSourceImpl,
    ): TraktRelatedShowsDataSource = bind
}

typealias TmdbRelatedShowsDataSource = RelatedShowsDataSource
typealias TraktRelatedShowsDataSource = RelatedShowsDataSource
