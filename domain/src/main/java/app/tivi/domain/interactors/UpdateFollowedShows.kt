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

import app.tivi.data.daos.TiviShowDao
import app.tivi.data.entities.RefreshType
import app.tivi.data.fetch
import app.tivi.data.repositories.episodes.SeasonsEpisodesRepository
import app.tivi.data.repositories.followedshows.FollowedShowsRepository
import app.tivi.data.repositories.showimages.ShowImagesStore
import app.tivi.data.repositories.shows.ShowStore
import app.tivi.domain.Interactor
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.Logger
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateFollowedShows @Inject constructor(
    private val followedShowsRepository: FollowedShowsRepository,
    private val seasonEpisodeRepository: SeasonsEpisodesRepository,
    private val showStore: ShowStore,
    private val showDao: TiviShowDao,
    private val showImagesStore: ShowImagesStore,
    private val logger: Logger,
    private val dispatchers: AppCoroutineDispatchers
) : Interactor<UpdateFollowedShows.Params>() {

    override suspend fun doWork(params: Params) {
        withContext(dispatchers.io) {
            if (params.forceRefresh || followedShowsRepository.needFollowedShowsSync()) {
                followedShowsRepository.syncFollowedShows()
            }

            // Finally sync the seasons/episodes and watches
            followedShowsRepository.getFollowedShows().forEach { entry ->
                ensureActive()

                showStore.fetch(entry.showId)
                try {
                    showImagesStore.fetch(entry.showId)
                } catch (t: Throwable) {
                    logger.e(t, "Error while fetching images for show: ${entry.showId}")
                }

                ensureActive()
                // Download the seasons + episodes
                if (params.forceRefresh || seasonEpisodeRepository.needShowSeasonsUpdate(entry.showId)) {
                    try {
                        seasonEpisodeRepository.updateSeasonsEpisodes(entry.showId)
                    } catch (t: Throwable) {
                        logger.e(t, "Error while updating show seasons/episodes: ${entry.showId}")
                    }
                }

                ensureActive()
                try {
                    seasonEpisodeRepository.updateShowEpisodeWatches(
                        showId = entry.showId,
                        refreshType = params.type,
                        forceRefresh = params.forceRefresh,
                        lastUpdated = showDao.getShowWithId(entry.showId)?.traktDataUpdate
                    )
                } catch (t: Throwable) {
                    logger.e(t, "Error while updating show episode watches: ${entry.showId}")
                }
            }
        }
    }

    data class Params(val forceRefresh: Boolean, val type: RefreshType)
}
