/*
 * Copyright 2017 Google, Inc.
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

package app.tivi.home.discover

import app.tivi.data.entities.TiviShow
import app.tivi.data.resultentities.PopularEntryWithShow
import app.tivi.data.resultentities.TrendingEntryWithShow
import app.tivi.tmdb.TmdbImageUrlProvider

sealed class DiscoverViewState(
    open val tmdbImageUrlProvider: TmdbImageUrlProvider,
    open val isLoading: Boolean
)

data class EmptyDiscoverViewState(
    val trendingItems: List<TrendingEntryWithShow>,
    val popularItems: List<PopularEntryWithShow>,
    override val tmdbImageUrlProvider: TmdbImageUrlProvider,
    override val isLoading: Boolean
) : DiscoverViewState(tmdbImageUrlProvider, isLoading)

data class SearchResultDiscoverViewState(
    val query: String,
    val results: List<TiviShow>,
    override val tmdbImageUrlProvider: TmdbImageUrlProvider,
    override val isLoading: Boolean
) : DiscoverViewState(tmdbImageUrlProvider, isLoading)