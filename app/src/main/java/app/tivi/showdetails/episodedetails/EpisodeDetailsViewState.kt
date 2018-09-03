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

package app.tivi.showdetails.episodedetails

import app.tivi.data.entities.Episode
import app.tivi.data.entities.EpisodeWatchEntry
import app.tivi.tmdb.TmdbImageUrlProvider
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.Uninitialized

data class EpisodeDetailsViewState(
    val episodeId: Long,
    val episode: Async<Episode> = Uninitialized,
    val watches: Async<List<EpisodeWatchEntry>> = Uninitialized,
    val tmdbImageUrlProvider: Async<TmdbImageUrlProvider> = Uninitialized,
    val action: Action = Action.WATCH
) : MvRxState {
    constructor(args: EpisodeDetailsFragment.Arguments) : this(args.episodeId)

    enum class Action {
        WATCH, UNWATCH
    }
}