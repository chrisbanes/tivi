/*
 * Copyright 2018 Google, Inc.
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

package app.tivi.showdetails.details

import app.tivi.data.entities.TiviShow
import app.tivi.data.resultentities.RelatedShowEntryWithShow
import app.tivi.data.resultentities.SeasonWithEpisodesAndWatches
import app.tivi.tmdb.TmdbImageUrlProvider

sealed class ShowDetailsViewState(
    open val show: TiviShow,
    open val relatedShows: List<RelatedShowEntryWithShow>,
    open val tmdbImageUrlProvider: TmdbImageUrlProvider
)

data class FollowedShowDetailsViewState(
    override val show: TiviShow,
    override val relatedShows: List<RelatedShowEntryWithShow>,
    val seasons: List<SeasonWithEpisodesAndWatches>,
    override val tmdbImageUrlProvider: TmdbImageUrlProvider
) : ShowDetailsViewState(show, relatedShows, tmdbImageUrlProvider)

data class NotFollowedShowDetailsViewState(
    override val show: TiviShow,
    override val relatedShows: List<RelatedShowEntryWithShow>,
    override val tmdbImageUrlProvider: TmdbImageUrlProvider
) : ShowDetailsViewState(show, relatedShows, tmdbImageUrlProvider)