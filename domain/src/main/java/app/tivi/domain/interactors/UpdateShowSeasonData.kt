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

import app.tivi.data.repositories.episodes.SeasonsEpisodesRepository
import app.tivi.data.repositories.followedshows.FollowedShowsRepository
import app.tivi.domain.Interactor
import app.tivi.domain.interactors.UpdateShowSeasonData.Params
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateShowSeasonData @Inject constructor(
    private val seasonsEpisodesRepository: SeasonsEpisodesRepository,
    private val followedShowsRepository: FollowedShowsRepository,
    private val dispatchers: AppCoroutineDispatchers
) : Interactor<Params>() {
    override suspend fun doWork(params: Params) {
        withContext(dispatchers.io) {
            if (followedShowsRepository.isShowFollowed(params.showId)) {
                // Then update the seasons/episodes
                if (params.forceRefresh || seasonsEpisodesRepository.needShowSeasonsUpdate(params.showId)) {
                    seasonsEpisodesRepository.updateSeasonsEpisodes(params.showId)
                }

                ensureActive()
                // Finally update any watched progress
                if (params.forceRefresh || seasonsEpisodesRepository.needShowEpisodeWatchesSync(params.showId)) {
                    seasonsEpisodesRepository.syncEpisodeWatchesForShow(params.showId)
                }
            } else {
                seasonsEpisodesRepository.removeShowSeasonData(params.showId)
            }
        }
    }

    data class Params(val showId: Long, val forceRefresh: Boolean)
}
