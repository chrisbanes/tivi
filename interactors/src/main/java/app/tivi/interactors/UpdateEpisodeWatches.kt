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

import app.tivi.data.entities.EpisodeWatchEntry
import app.tivi.data.repositories.episodes.SeasonsEpisodesRepository
import app.tivi.extensions.emptyFlowableList
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.AppRxSchedulers
import io.reactivex.Flowable
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class UpdateEpisodeWatches @Inject constructor(
    private val seasonsEpisodesRepository: SeasonsEpisodesRepository,
    dispatchers: AppCoroutineDispatchers,
    private val schedulers: AppRxSchedulers
) : SubjectInteractor<UpdateEpisodeWatches.Params, UpdateEpisodeWatches.ExecuteParams, List<EpisodeWatchEntry>>() {
    override val dispatcher: CoroutineDispatcher = dispatchers.io

    override fun createObservable(params: Params): Flowable<List<EpisodeWatchEntry>> {
        return seasonsEpisodesRepository.observeEpisodeWatches(params.episodeId)
                .startWith(emptyFlowableList())
                .subscribeOn(schedulers.io)
    }

    override suspend fun execute(params: Params, executeParams: ExecuteParams) {
        // TODO add refresh?
        // Don't do anything here
    }

    data class Params(val episodeId: Long)

    data class ExecuteParams(val forceLoad: Boolean)
}