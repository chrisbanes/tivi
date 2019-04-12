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

package app.tivi.data.repositories.relatedshows

import app.tivi.data.RetrofitRunner
import app.tivi.data.entities.RelatedShowEntry
import app.tivi.data.entities.Result
import app.tivi.data.entities.TiviShow
import app.tivi.data.mappers.IndexedMapper
import app.tivi.data.mappers.ShowIdToTraktIdMapper
import app.tivi.data.mappers.TraktShowToTiviShow
import app.tivi.data.mappers.pairMapperOf
import app.tivi.extensions.executeWithRetry
import com.uwetrottmann.trakt5.entities.Show
import com.uwetrottmann.trakt5.enums.Extended
import com.uwetrottmann.trakt5.services.Shows
import javax.inject.Inject
import javax.inject.Provider

class TraktRelatedShowsDataSource @Inject constructor(
    private val traktIdMapper: ShowIdToTraktIdMapper,
    private val showService: Provider<Shows>,
    private val retrofitRunner: RetrofitRunner,
    private val showMapper: TraktShowToTiviShow
) : RelatedShowsDataSource {
    private val entryMapper = object : IndexedMapper<Show, RelatedShowEntry> {
        override suspend fun map(index: Int, from: Show): RelatedShowEntry {
            return RelatedShowEntry(showId = 0, otherShowId = 0, orderIndex = index)
        }
    }
    private val resultMapper = pairMapperOf(showMapper, entryMapper)

    override suspend fun getRelatedShows(showId: Long): Result<List<Pair<TiviShow, RelatedShowEntry>>> {
        return retrofitRunner.executeForResponse(resultMapper) {
            showService.get().related(traktIdMapper.map(showId).toString(), 0, 10, Extended.NOSEASONS)
                    .executeWithRetry()
        }
    }
}