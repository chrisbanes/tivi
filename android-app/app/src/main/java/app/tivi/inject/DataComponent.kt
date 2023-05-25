/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
