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

import app.tivi.data.entities.SearchResults
import app.tivi.data.entities.TraktUser
import app.tivi.data.resultentities.PopularEntryWithShow
import app.tivi.data.resultentities.TrendingEntryWithShow
import app.tivi.tmdb.TmdbImageUrlProvider
import app.tivi.trakt.TraktAuthState
import com.airbnb.mvrx.MvRxState

data class DiscoverViewState(
    val isSearchOpen: Boolean = false,
    val searchResults: SearchResults? = null,
    val trendingItems: List<TrendingEntryWithShow> = emptyList(),
    val popularItems: List<PopularEntryWithShow> = emptyList(),
    val tmdbImageUrlProvider: TmdbImageUrlProvider = TmdbImageUrlProvider(),
    val isLoading: Boolean = false,
    val user: TraktUser? = null,
    val authState: TraktAuthState = TraktAuthState.LOGGED_OUT
) : MvRxState