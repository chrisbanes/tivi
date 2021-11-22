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

import androidx.compose.runtime.Immutable
import app.tivi.data.entities.ShowTmdbImage
import app.tivi.data.entities.TiviShow
import app.tivi.data.resultentities.EpisodeWithSeason
import app.tivi.data.resultentities.RelatedShowEntryWithShow
import app.tivi.data.resultentities.SeasonWithEpisodesAndWatches
import app.tivi.data.views.FollowedShowsWatchStats

@Immutable
internal data class ShowDetailsViewState(
    val isFollowed: Boolean,
    val show: TiviShow,
    val posterImage: ShowTmdbImage?,
    val backdropImage: ShowTmdbImage?,
    val relatedShows: List<RelatedShowEntryWithShow>,
    val nextEpisodeToWatch: EpisodeWithSeason?,
    val watchStats: FollowedShowsWatchStats?,
    val seasons: List<SeasonWithEpisodesAndWatches>,
    val refreshing: Boolean,
) {
    companion object {
        val Empty = ShowDetailsViewState(
            isFollowed = false,
            show = TiviShow.EMPTY_SHOW,
            posterImage = null,
            backdropImage = null,
            relatedShows = emptyList(),
            nextEpisodeToWatch = null,
            watchStats = null,
            seasons = emptyList(),
            refreshing = false,
        )
    }
}

internal sealed class ShowDetailsUiEffect {
    data class ShowError(val message: String) : ShowDetailsUiEffect() {
        constructor(throwable: Throwable?) : this("Error: ${throwable?.localizedMessage}")
    }

    object ClearError : ShowDetailsUiEffect()
}
