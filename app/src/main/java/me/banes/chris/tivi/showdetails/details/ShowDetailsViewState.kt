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

package me.banes.chris.tivi.showdetails.details

import me.banes.chris.tivi.data.entities.RelatedShowsListItem
import me.banes.chris.tivi.data.entities.SeasonWithEpisodes
import me.banes.chris.tivi.data.entities.TiviShow
import me.banes.chris.tivi.tmdb.TmdbImageUrlProvider

sealed class ShowDetailsViewState(
    open val show: TiviShow,
    open val relatedShows: List<RelatedShowsListItem>,
    open val tmdbImageUrlProvider: TmdbImageUrlProvider
)

data class FollowedShowDetailsViewState(
    override val show: TiviShow,
    override val relatedShows: List<RelatedShowsListItem>,
    val seasons: List<SeasonWithEpisodes>,
    override val tmdbImageUrlProvider: TmdbImageUrlProvider
) : ShowDetailsViewState(show, relatedShows, tmdbImageUrlProvider)

data class NotFollowedShowDetailsViewState(
    override val show: TiviShow,
    override val relatedShows: List<RelatedShowsListItem>,
    override val tmdbImageUrlProvider: TmdbImageUrlProvider
) : ShowDetailsViewState(show, relatedShows, tmdbImageUrlProvider)