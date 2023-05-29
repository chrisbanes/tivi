// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi

import app.tivi.account.AccountUi
import app.tivi.episode.track.EpisodeTrack
import app.tivi.episodedetails.EpisodeDetails
import app.tivi.home.discover.Discover
import app.tivi.home.library.Library
import app.tivi.home.popular.PopularShows
import app.tivi.home.recommended.RecommendedShows
import app.tivi.home.search.Search
import app.tivi.home.trending.TrendingShows
import app.tivi.home.upnext.UpNext
import app.tivi.showdetails.details.ShowDetails
import app.tivi.showdetails.seasons.ShowSeasons
import me.tatarka.inject.annotations.Inject

@Inject
class ComposeScreens(
    val accountUi: AccountUi,
    val discover: Discover,
    val episodeDetails: EpisodeDetails,
    val episodeTrack: EpisodeTrack,
    val library: Library,
    val popularShows: PopularShows,
    val recommendedShows: RecommendedShows,
    val search: Search,
    val showDetails: ShowDetails,
    val showSeasons: ShowSeasons,
    val trendingShows: TrendingShows,
    val upNext: UpNext,
)
