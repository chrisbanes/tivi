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
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import org.threeten.bp.OffsetDateTime
import javax.inject.Inject

class AddEpisodeWatch @Inject constructor(
    dispatchers: AppCoroutineDispatchers,
    private val seasonsEpisodesRepository: SeasonsEpisodesRepository
) : Interactor<AddEpisodeWatch.Params> {
    override val dispatcher: CoroutineDispatcher = dispatchers.io

    override suspend operator fun invoke(params: Params) {
        seasonsEpisodesRepository.markEpisodeWatched(params.episodeId, params.timestamp)
    }

    data class Params(val episodeId: Long, val timestamp: OffsetDateTime)
}