// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.recommendedshows

import me.tatarka.inject.annotations.Provides

interface RecommendedShowsBinds {
    @Provides
    fun provideTraktRecommendedShowsDataSource(
        bind: TraktRecommendedShowsDataSource,
    ): RecommendedShowsDataSource = bind
}
