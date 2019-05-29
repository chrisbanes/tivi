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
import app.tivi.extensions.emptyObservableList
import app.tivi.util.AppRxSchedulers
import io.reactivex.Observable
import javax.inject.Inject

class ObserveEpisodeWatches @Inject constructor(
    private val seasonsEpisodesRepository: SeasonsEpisodesRepository,
    private val schedulers: AppRxSchedulers
) : SubjectInteractor<ObserveEpisodeWatches.Params, List<EpisodeWatchEntry>>() {
    override fun createObservable(params: Params): Observable<List<EpisodeWatchEntry>> {
        return seasonsEpisodesRepository.observeEpisodeWatches(params.episodeId)
                .toObservable()
                .startWith(emptyObservableList())
                .subscribeOn(schedulers.io)
    }

    data class Params(val episodeId: Long)
}