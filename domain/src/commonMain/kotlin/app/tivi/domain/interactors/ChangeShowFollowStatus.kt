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

import app.tivi.data.followedshows.FollowedShowsRepository
import app.tivi.data.shows.ShowStore
import app.tivi.data.util.fetch
import app.tivi.domain.Interactor
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.parallelForEach
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class ChangeShowFollowStatus(
    private val followedShowsRepository: FollowedShowsRepository,
    private val showStore: ShowStore,
    private val dispatchers: AppCoroutineDispatchers,
) : Interactor<ChangeShowFollowStatus.Params>() {
    override suspend fun doWork(params: Params) {
        withContext(dispatchers.io) {
            params.showIds.forEach { showId ->
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
            val result = followedShowsRepository.syncFollowedShows()

            result.added.parallelForEach {
                showStore.fetch(it.showId)
            }
        }
    }

    private suspend fun unfollow(showId: Long) {
        followedShowsRepository.removeFollowedShow(showId)
    }

    private suspend fun follow(showId: Long) {
        followedShowsRepository.addFollowedShow(showId)
    }

    data class Params(
        val showIds: Collection<Long>,
        val action: Action,
    ) {
        constructor(showId: Long, action: Action) : this(listOf(showId), action)
    }

    enum class Action { FOLLOW, UNFOLLOW, TOGGLE }
}
