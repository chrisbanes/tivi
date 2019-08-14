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
import app.tivi.domain.Interactor
import app.tivi.domain.interactors.UpdateShowSeasons.Params
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class UpdateShowSeasons @Inject constructor(
    private val seasonsEpisodesRepository: SeasonsEpisodesRepository,
    dispatchers: AppCoroutineDispatchers
) : Interactor<Params> {
    override val dispatcher: CoroutineDispatcher = dispatchers.io

    override suspend fun invoke(params: Params) {
        seasonsEpisodesRepository.updateSeasonsEpisodes(params.showId)
    }

    data class Params(val showId: Long)
}