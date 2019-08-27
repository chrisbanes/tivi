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
import app.tivi.data.repositories.episodes.SeasonsEpisodesRepository
import app.tivi.data.repositories.followedshows.FollowedShowsRepository
import app.tivi.domain.Interactor
import app.tivi.inject.ProcessLifetime
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import javax.inject.Inject

class ChangeShowFollowStatus @Inject constructor(
    private val followedShowsRepository: FollowedShowsRepository,
    private val seasonsEpisodesRepository: SeasonsEpisodesRepository,
    dispatchers: AppCoroutineDispatchers,
    private val showTasks: ShowTasks,
    @ProcessLifetime val processScope: CoroutineScope
) : Interactor<ChangeShowFollowStatus.Params>() {
    override val scope: CoroutineScope = processScope + dispatchers.io

    override suspend fun doWork(params: Params) {
        suspend fun unfollow(showId: Long) {
            followedShowsRepository.removeFollowedShow(showId)
            // Remove seasons, episodes and watches
            seasonsEpisodesRepository.removeShowSeasonData(showId)
        }

        suspend fun follow(showId: Long) {
            followedShowsRepository.addFollowedShow(showId)
            // Update seasons, episodes and watches
            if (!params.deferDataFetch) {
                seasonsEpisodesRepository.updateSeasonsEpisodes(showId)
                seasonsEpisodesRepository.updateShowEpisodeWatches(showId, forceRefresh = true)
            }
        }

        for (showId in params.showIds) {
            when (params.action) {
                Action.TOGGLE -> {
                    if (followedShowsRepository.isShowFollowed(showId)) {
                        unfollow(showId)
                    } else {
                        follow(showId)
                    }
                }
                Action.FOLLOW -> follow(showId)
                Action.UNFOLLOW -> unfollow(showId)
            }
        }
        // Finally, sync the changes to Trakt
        followedShowsRepository.syncFollowedShows()

        if (params.deferDataFetch) {
            showTasks.syncFollowedShows()
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