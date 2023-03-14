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

import app.tivi.data.daos.WatchedShowDao
import app.tivi.data.episodes.SeasonsEpisodesRepository
import app.tivi.data.followedshows.FollowedShowsRepository
import app.tivi.data.shows.ShowStore
import app.tivi.data.util.fetch
import app.tivi.data.watchedshows.WatchedShowsLastRequestStore
import app.tivi.data.watchedshows.WatchedShowsStore
import app.tivi.domain.Interactor
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.Logger
import app.tivi.util.parallelForEach
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.async
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class UpdateLibraryShows(
    private val followedShowsRepository: FollowedShowsRepository,
    private val seasonEpisodeRepository: SeasonsEpisodesRepository,
    private val showStore: ShowStore,
    private val watchedShowsLastRequestStore: WatchedShowsLastRequestStore,
    private val watchedShowsStore: WatchedShowsStore,
    private val watchedShowDao: WatchedShowDao,
    private val logger: Logger,
    private val dispatchers: AppCoroutineDispatchers,
) : Interactor<UpdateLibraryShows.Params>() {

    override suspend fun doWork(params: Params): Unit = withContext(dispatchers.io) {
        val watchedShowsDeferred = async {
            // We use a low threshold here as the data contains a 'last updated' value.
            // It's a quick way to know whether to cascade the updates below
            watchedShowsStore.fetch(
                key = Unit,
                forceFresh = params.forceRefresh ||
                    watchedShowsLastRequestStore.isRequestExpired(1.hours),
            )
        }
        val followedShowsJob = launch {
            if (params.forceRefresh || followedShowsRepository.needFollowedShowsSync()) {
                followedShowsRepository.syncFollowedShows()
            }
        }

        // await the watched shows and followed shows update. We need both
        followedShowsJob.join()
        // Finally sync the seasons/episodes and watches
        watchedShowsDeferred.await().parallelForEach { entry ->
            ensureActive()
            showStore.fetch(entry.showId)

            ensureActive()
            try {
                with(seasonEpisodeRepository) {
                    val watchedEntry = watchedShowDao.entryWithShowId(entry.showId)

                    if (needShowSeasonsUpdate(entry.showId, watchedEntry?.lastUpdated)) {
                        updateSeasonsEpisodes(entry.showId)
                    }

                    ensureActive()

                    if (needShowEpisodeWatchesSync(entry.showId, watchedEntry?.lastUpdated)) {
                        updateShowEpisodeWatches(entry.showId)
                    }
                }
            } catch (t: Throwable) {
                logger.e(t, "Error while updating show seasons/episodes: ${entry.showId}")
            }
        }
    }

    data class Params(val forceRefresh: Boolean)
}
