// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.trendingshows

import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Provides

interface TrendingShowsBinds {
    @Provides
    @ApplicationScope
    fun provideTraktTrendingShowsDataSource(
        bind: TraktTrendingShowsDataSource,
    ): TrendingShowsDataSource = bind
}
