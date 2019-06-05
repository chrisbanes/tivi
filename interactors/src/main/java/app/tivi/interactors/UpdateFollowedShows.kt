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

package app.tivi.interactors

import app.tivi.data.instantInPast
import app.tivi.data.repositories.episodes.SeasonsEpisodesRepository
import app.tivi.data.repositories.followedshows.FollowedShowsRepository
import app.tivi.data.repositories.watchedshows.WatchedShowsRepository
import app.tivi.extensions.parallelForEach
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class UpdateFollowedShows @Inject constructor(
    dispatchers: AppCoroutineDispatchers,
    private val followedShowsRepository: FollowedShowsRepository,
    private val watchedShowsRepository: WatchedShowsRepository,
    private val seasonEpisodeRepository: SeasonsEpisodesRepository
) : Interactor<UpdateFollowedShows.Params> {
    override val dispatcher: CoroutineDispatcher = dispatchers.io

    override suspend fun invoke(params: Params) = coroutineScope {
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
        followedShowsRepository.getFollowedShows().parallelForEach {
            // Download the seasons + episodes
            if (params.forceRefresh || seasonEpisodeRepository.needShowSeasonsUpdate(it.showId)) {
                seasonEpisodeRepository.updateSeasonsEpisodes(it.showId)
            }

            // And sync the episode watches
            if (params.type == RefreshType.QUICK) {
                val showWatchedEntry = watchedShowsRepository.getWatchedShow(it.showId)
                if (showWatchedEntry != null) {
                    // TODO: We should really use last_updated_at. Waiting on trakt-java support in
                    // https://github.com/UweTrottmann/trakt-java/pull/106
                    val lastWatchUpdate = showWatchedEntry.lastWatched

                    if (params.forceRefresh || seasonEpisodeRepository.needShowEpisodeWatchesSync(
                                    it.showId, lastWatchUpdate.toInstant())) {
                        seasonEpisodeRepository.updateShowEpisodeWatches(it.showId,
                                lastWatchUpdate.plusSeconds(1))
                    }
                } else {
                    // We don't have a trakt date/time to use as a delta, so we'll do a full refresh.
                    // If the user hasn't watched the show, this should be empty anyway
                    if (params.forceRefresh || seasonEpisodeRepository.needShowEpisodeWatchesSync(it.showId)) {
                        seasonEpisodeRepository.updateShowEpisodeWatches(it.showId)
                    }
                }
            } else if (params.type == RefreshType.FULL) {
                // A full refresh is requested, so we pull down all history
                if (params.forceRefresh || seasonEpisodeRepository.needShowEpisodeWatchesSync(it.showId)) {
                    seasonEpisodeRepository.updateShowEpisodeWatches(it.showId)
                }
            }
        }
    }

    data class Params(val forceRefresh: Boolean, val type: RefreshType)

    enum class RefreshType { QUICK, FULL }
}