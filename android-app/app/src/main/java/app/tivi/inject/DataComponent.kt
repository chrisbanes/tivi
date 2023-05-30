// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.inject

import app.tivi.data.AndroidSqlDelightDatabaseComponent
import app.tivi.data.episodes.EpisodeBinds
import app.tivi.data.followedshows.FollowedShowsBinds
import app.tivi.data.popularshows.PopularShowsBinds
import app.tivi.data.recommendedshows.RecommendedShowsBinds
import app.tivi.data.search.SearchBinds
import app.tivi.data.showimages.ShowImagesBinds
import app.tivi.data.shows.ShowsBinds
import app.tivi.data.traktauth.RelatedShowsBinds
import app.tivi.data.traktauth.TraktAuthComponent
import app.tivi.data.traktusers.TraktUsersBinds
import app.tivi.data.trendingshows.TrendingShowsBinds
import app.tivi.data.watchedshows.WatchedShowsBinds

interface DataComponent :
    EpisodeBinds,
    FollowedShowsBinds,
    PopularShowsBinds,
    RecommendedShowsBinds,
    RelatedShowsBinds,
    SearchBinds,
    ShowImagesBinds,
    ShowsBinds,
    TraktAuthComponent,
    TraktUsersBinds,
    TrendingShowsBinds,
    WatchedShowsBinds,
    AndroidSqlDelightDatabaseComponent
