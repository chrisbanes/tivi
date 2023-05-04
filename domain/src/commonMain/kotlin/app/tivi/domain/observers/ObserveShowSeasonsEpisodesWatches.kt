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

package app.tivi.domain.observers

import app.tivi.data.compoundmodels.SeasonWithEpisodesAndWatches
import app.tivi.data.episodes.SeasonsEpisodesRepository
import app.tivi.domain.SubjectInteractor
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
class ObserveShowSeasonsEpisodesWatches(
    private val seasonsEpisodesRepository: SeasonsEpisodesRepository,
) : SubjectInteractor<ObserveShowSeasonsEpisodesWatches.Params, List<SeasonWithEpisodesAndWatches>>() {

    override fun createObservable(params: Params): Flow<List<SeasonWithEpisodesAndWatches>> {
        return seasonsEpisodesRepository.observeSeasonsWithEpisodesWatchedForShow(params.showId)
    }

    data class Params(val showId: Long)
}
