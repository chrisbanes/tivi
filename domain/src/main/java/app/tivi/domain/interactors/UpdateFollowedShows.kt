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

import app.tivi.data.cachedOnly
import app.tivi.data.entities.RefreshType
import app.tivi.data.fetch
import app.tivi.data.fetchCollection
import app.tivi.data.repositories.episodes.SeasonsEpisodesRepository
import app.tivi.data.repositories.followedshows.FollowedShowsRepository
import app.tivi.data.repositories.showimages.ShowImagesStore
import app.tivi.data.repositories.shows.ShowStore
import app.tivi.domain.Interactor
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateFollowedShows @Inject constructor(
    private val followedShowsRepository: FollowedShowsRepository,
    private val seasonEpisodeRepository: SeasonsEpisodesRepository,
    private val showStore: ShowStore,
    private val showImagesStore: ShowImagesStore,
    private val dispatchers: AppCoroutineDispatchers
) : Interactor<UpdateFollowedShows.Params>() {

    override suspend fun doWork(params: Params) {
        withContext(dispatchers.io) {
            if (params.forceRefresh || followedShowsRepository.needFollowedShowsSync()) {
                followedShowsRepository.syncFollowedShows()
            }

            // Finally sync the seasons/episodes and watches
            followedShowsRepository.getFollowedShows().asFlow().collect {
                showStore.fetch(it.showId)
                showImagesStore.fetchCollection(it.showId)

                ensureActive()
                // Download the seasons + episodes
                if (params.forceRefresh || seasonEpisodeRepository.needShowSeasonsUpdate(it.showId)) {
                    seasonEpisodeRepository.updateSeasonsEpisodes(it.showId)
                }

                ensureActive()
                seasonEpisodeRepository.updateShowEpisodeWatches(
                    it.showId,
                    params.type,
                    params.forceRefresh,
                    showStore.cachedOnly(it.showId)?.traktDataUpdate
                )
            }
        }
    }

    data class Params(val forceRefresh: Boolean, val type: RefreshType)
}
