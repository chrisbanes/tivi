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

import app.tivi.actions.ShowTasks
import app.tivi.data.fetch
import app.tivi.data.repositories.episodes.SeasonsEpisodesRepository
import app.tivi.data.repositories.followedshows.FollowedShowsRepository
import app.tivi.data.repositories.showimages.ShowImagesStore
import app.tivi.data.repositories.shows.ShowStore
import app.tivi.domain.Interactor
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.Logger
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ChangeShowFollowStatus @Inject constructor(
    private val followedShowsRepository: FollowedShowsRepository,
    private val seasonsEpisodesRepository: SeasonsEpisodesRepository,
    private val showStore: ShowStore,
    private val showImagesStore: ShowImagesStore,
    private val dispatchers: AppCoroutineDispatchers,
    private val showTasks: ShowTasks,
    private val logger: Logger,
) : Interactor<ChangeShowFollowStatus.Params>() {
    override suspend fun doWork(params: Params) {
        withContext(dispatchers.io) {
            params.showIds.forEach { showId ->
                when (params.action) {
                    Action.TOGGLE -> {
                        if (followedShowsRepository.isShowFollowed(showId)) {
                            unfollow(showId)
                        } else {
                            follow(showId, params.deferDataFetch)
                        }
                    }
                    Action.FOLLOW -> follow(showId, params.deferDataFetch)
                    Action.UNFOLLOW -> unfollow(showId)
                }
            }
            // Finally, sync the changes to Trakt
            val result = followedShowsRepository.syncFollowedShows()

            result.added.forEach {
                showStore.fetch(it.showId)
                try {
                    showImagesStore.fetch(it.showId)
                } catch (t: Throwable) {
                    logger.e(t, "Error while fetching image")
                }
            }

            if (params.deferDataFetch) {
                showTasks.syncFollowedShows()
            }
        }
    }

    private suspend fun unfollow(showId: Long) {
        followedShowsRepository.removeFollowedShow(showId)
        // Remove seasons, episodes and watches
        seasonsEpisodesRepository.removeShowSeasonData(showId)
    }

    private suspend fun follow(showId: Long, deferDataFetch: Boolean) {
        followedShowsRepository.addFollowedShow(showId)
        // Update seasons, episodes and watches now if we're not deferring the fetch
        if (!deferDataFetch) {
            seasonsEpisodesRepository.updateSeasonsEpisodes(showId)
            seasonsEpisodesRepository.updateShowEpisodeWatches(showId, forceRefresh = true)
        }
    }

    data class Params(
        val showIds: Collection<Long>,
        val action: Action,
        val deferDataFetch: Boolean = false
    ) {
        constructor(showId: Long, action: Action) : this(listOf(showId), action)
    }

    enum class Action { FOLLOW, UNFOLLOW, TOGGLE }
}
