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

import app.tivi.data.entities.ActionDate
import app.tivi.data.repositories.episodes.SeasonsEpisodesRepository
import app.tivi.domain.Interactor
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ChangeSeasonWatchedStatus @Inject constructor(
    private val seasonsEpisodesRepository: SeasonsEpisodesRepository,
    private val dispatchers: AppCoroutineDispatchers
) : Interactor<ChangeSeasonWatchedStatus.Params>() {

    override suspend fun doWork(params: Params) {
        withContext(dispatchers.io) {
            when (params.action) {
                Action.WATCHED -> {
                    seasonsEpisodesRepository.markSeasonWatched(
                        params.seasonId,
                        params.onlyAired,
                        params.actionDate
                    )
                }
                Action.UNWATCH -> {
                    seasonsEpisodesRepository.markSeasonUnwatched(params.seasonId)
                }
            }
        }
    }

    data class Params(
        val seasonId: Long,
        val action: Action,
        val onlyAired: Boolean = true,
        val actionDate: ActionDate = ActionDate.NOW
    )

    enum class Action { WATCHED, UNWATCH }
}
