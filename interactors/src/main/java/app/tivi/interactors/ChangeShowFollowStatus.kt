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

import app.tivi.data.repositories.followedshows.FollowedShowsRepository
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.Logger
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class ChangeShowFollowStatus @Inject constructor(
    dispatchers: AppCoroutineDispatchers,
    private val followedShowsRepository: FollowedShowsRepository,
    private val logger: Logger
) : Interactor<ChangeShowFollowStatus.Params> {
    override val dispatcher: CoroutineDispatcher = dispatchers.io

    override suspend fun invoke(params: Params) {
        logger.d("ChangeShowFollowStatus started: %s", params)
        when (params.action) {
            Action.TOGGLE -> followedShowsRepository.toggleFollowedShow(params.showId)
            Action.FOLLOW -> followedShowsRepository.addFollowedShow(params.showId)
            Action.UNFOLLOW -> followedShowsRepository.removeFollowedShow(params.showId)
        }
    }

    data class Params(val showId: Long, val action: Action)

    enum class Action { FOLLOW, UNFOLLOW, TOGGLE }
}