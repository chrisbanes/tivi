// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.trakt

import app.moviebase.trakt.Trakt
import app.moviebase.trakt.api.TraktEpisodesApi
import app.moviebase.trakt.api.TraktRecommendationsApi
import app.moviebase.trakt.api.TraktSearchApi
import app.moviebase.trakt.api.TraktSeasonsApi
import app.moviebase.trakt.api.TraktShowsApi
import app.moviebase.trakt.api.TraktSyncApi
import app.moviebase.trakt.api.TraktUsersApi
import me.tatarka.inject.annotations.Provides

interface TraktComponent : TraktCommonComponent, TraktPlatformComponent

expect interface TraktPlatformComponent

interface TraktCommonComponent {
    @Provides
    fun provideTraktUsersService(trakt: Trakt): TraktUsersApi = trakt.users

    @Provides
    fun provideTraktShowsService(trakt: Trakt): TraktShowsApi = trakt.shows

    @Provides
    fun provideTraktEpisodesService(trakt: Trakt): TraktEpisodesApi = trakt.episodes

    @Provides
    fun provideTraktSeasonsService(trakt: Trakt): TraktSeasonsApi = trakt.seasons

    @Provides
    fun provideTraktSyncService(trakt: Trakt): TraktSyncApi = trakt.sync

    @Provides
    fun provideTraktSearchService(trakt: Trakt): TraktSearchApi = trakt.search

    @Provides
    fun provideTraktRecommendationsService(trakt: Trakt): TraktRecommendationsApi = trakt.recommendations
}
