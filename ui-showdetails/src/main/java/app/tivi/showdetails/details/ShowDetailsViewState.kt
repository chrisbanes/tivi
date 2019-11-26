/*
 * Copyright 2018 Google LLC
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

import app.tivi.data.entities.ShowTmdbImage
import app.tivi.data.entities.TiviShow
import app.tivi.data.resultentities.EpisodeWithSeason
import app.tivi.data.resultentities.RelatedShowEntryWithShow
import app.tivi.data.resultentities.SeasonWithEpisodesAndWatches
import app.tivi.data.views.FollowedShowsWatchStats
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.Uninitialized

data class ShowDetailsViewState(
    val showId: Long = 0,
    val isFollowed: Boolean = false,
    val show: TiviShow = TiviShow.EMPTY_SHOW,
    val posterImage: ShowTmdbImage? = null,
    val backdropImage: ShowTmdbImage? = null,
    val relatedShows: Async<List<RelatedShowEntryWithShow>> = Uninitialized,
    val nextEpisodeToWatch: Async<EpisodeWithSeason?> = Uninitialized,
    val viewStats: Async<FollowedShowsWatchStats> = Uninitialized,
    val seasons: Async<List<SeasonWithEpisodesAndWatches>> = Uninitialized,
    val expandedSeasonIds: Set<Long> = emptySet(),
    val focusedSeason: FocusSeasonUiEffect? = null,
    val openEpisodeUiEffect: OpenEpisodeUiEffect? = null,
    val refreshing: Boolean = false
) : MvRxState

data class FocusSeasonUiEffect(val seasonId: Long)

sealed class OpenEpisodeUiEffect
data class PendingOpenEpisodeUiEffect(val episodeId: Long) : OpenEpisodeUiEffect()
data class ExecutableOpenEpisodeUiEffect(val episodeId: Long, val seasonId: Long) : OpenEpisodeUiEffect()
