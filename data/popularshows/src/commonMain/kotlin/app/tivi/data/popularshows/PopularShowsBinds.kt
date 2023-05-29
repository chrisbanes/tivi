// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.popularshows

import me.tatarka.inject.annotations.Provides

interface PopularShowsBinds {
    @Provides
    fun provideTraktPopularShowsDataSource(
        bind: TraktPopularShowsDataSource,
    ): PopularShowsDataSource = bind
}
