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

package app.tivi.interactors

import android.arch.paging.DataSource
import app.tivi.data.repositories.episodes.SeasonsEpisodesRepository
import app.tivi.data.repositories.followedshows.FollowedShowsRepository
import app.tivi.data.resultentities.FollowedShowEntryWithShow
import app.tivi.extensions.parallelForEach
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.experimental.CoroutineDispatcher
import javax.inject.Inject

class SyncFollowedShows @Inject constructor(
    private val dispatchers: AppCoroutineDispatchers,
    private val followedShowsRepository: FollowedShowsRepository,
    private val repository: SeasonsEpisodesRepository
) : PagingInteractor<FollowedShowEntryWithShow>, Interactor<SyncFollowedShows.ExecuteParams> {
    override val dispatcher: CoroutineDispatcher = dispatchers.io

    override suspend fun invoke(executeParams: ExecuteParams) {
        if (executeParams.forceLoad || followedShowsRepository.needFollowedShowsSync()) {
            followedShowsRepository.syncFollowedShows()

            // Finally sync the watches
            followedShowsRepository.getFollowedShows().parallelForEach {
                // Download the seasons + episodes
                if (executeParams.forceLoad || repository.needShowSeasonsUpdate(it.showId)) {
                    repository.updateSeasonsEpisodes(it.showId)
                }
                // And sync the episode watches
                if (executeParams.forceLoad || repository.needShowEpisodeWatchesSync(it.showId)) {
                    repository.syncEpisodeWatchesForShow(it.showId)
                }
            }
        }
    }

    override fun dataSourceFactory(): DataSource.Factory<Int, FollowedShowEntryWithShow> {
        return followedShowsRepository.observeFollowedShows()
    }

    data class ExecuteParams(val forceLoad: Boolean)
}