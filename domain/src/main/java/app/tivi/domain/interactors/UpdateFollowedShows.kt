/*
 * Copyright 2019 Google LLC
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

package app.tivi.domain.interactors

import app.tivi.data.entities.RefreshType
import app.tivi.data.instantInPast
import app.tivi.data.repositories.episodes.SeasonsEpisodesRepository
import app.tivi.data.repositories.followedshows.FollowedShowsRepository
import app.tivi.data.repositories.shows.ShowRepository
import app.tivi.data.repositories.watchedshows.WatchedShowsRepository
import app.tivi.domain.Interactor
import app.tivi.extensions.parallelForEach
import app.tivi.inject.ProcessLifetime
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import javax.inject.Inject

class UpdateFollowedShows @Inject constructor(
    private val showRepository: ShowRepository,
    private val followedShowsRepository: FollowedShowsRepository,
    private val watchedShowsRepository: WatchedShowsRepository,
    private val seasonEpisodeRepository: SeasonsEpisodesRepository,
    dispatchers: AppCoroutineDispatchers,
    @ProcessLifetime val processScope: CoroutineScope
) : Interactor<UpdateFollowedShows.Params>() {
    override val scope: CoroutineScope = processScope + dispatchers.io

    override suspend fun doWork(params: Params) = coroutineScope {
        val syncFollowed = launch {
            if (params.forceRefresh || followedShowsRepository.needFollowedShowsSync()) {
                followedShowsRepository.syncFollowedShows()
            }
        }
        val syncWatched = launch {
            // Refresh the watched shows list with a short expiry
            if (params.forceRefresh || watchedShowsRepository.needUpdate(instantInPast(hours = 1))) {
                watchedShowsRepository.updateWatchedShows()
            }
        }

        syncFollowed.join()
        syncWatched.join()

        // Finally sync the seasons/episodes and watches
        followedShowsRepository.syncFollowedShows()
        followedShowsRepository.getFollowedShows().parallelForEach {
            // Download the seasons + episodes
            if (params.forceRefresh || seasonEpisodeRepository.needShowSeasonsUpdate(it.showId)) {
                seasonEpisodeRepository.updateSeasonsEpisodes(it.showId)
            }

            seasonEpisodeRepository.updateShowEpisodeWatchesIfNeeded(
                    it.showId,
                    params.type,
                    params.forceRefresh,
                    showRepository.getShow(it.showId)?.traktDataUpdate
            )
        }
    }

    data class Params(val forceRefresh: Boolean, val type: RefreshType)
}