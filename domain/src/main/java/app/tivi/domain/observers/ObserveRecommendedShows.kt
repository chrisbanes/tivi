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

import app.tivi.data.daos.RecommendedDao
import app.tivi.data.resultentities.RecommendedEntryWithShow
import app.tivi.domain.SubjectInteractor
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveRecommendedShows @Inject constructor(
    private val recommendedDao: RecommendedDao
) : SubjectInteractor<ObserveRecommendedShows.Params, List<RecommendedEntryWithShow>>() {

    override fun createObservable(
        params: ObserveRecommendedShows.Params
    ): Flow<List<RecommendedEntryWithShow>> {
        return recommendedDao.entriesObservable(params.count, 0)
    }

    data class Params(val count: Int = 20)
}
