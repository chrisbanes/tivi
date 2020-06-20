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

import app.tivi.data.daos.RelatedShowsDao
import app.tivi.data.resultentities.RelatedShowEntryWithShow
import app.tivi.domain.SubjectInteractor
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveRelatedShows @Inject constructor(
    private val relatedShowsDao: RelatedShowsDao
) : SubjectInteractor<ObserveRelatedShows.Params, List<RelatedShowEntryWithShow>>() {

    override fun createObservable(params: Params): Flow<List<RelatedShowEntryWithShow>> {
        return relatedShowsDao.entriesWithShowsObservable(params.showId)
    }

    data class Params(val showId: Long)
}
