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

package app.tivi.data.repositories.popularshows

import app.tivi.data.RetrofitRunner
import app.tivi.data.entities.PopularShowEntry
import app.tivi.data.entities.Result
import app.tivi.data.entities.TiviShow
import app.tivi.data.mappers.IndexedMapper
import app.tivi.data.mappers.TraktShowToTiviShow
import app.tivi.data.mappers.pairMapperOf
import app.tivi.extensions.executeWithRetry
import com.uwetrottmann.trakt5.entities.Show
import com.uwetrottmann.trakt5.enums.Extended
import com.uwetrottmann.trakt5.services.Shows
import javax.inject.Inject
import javax.inject.Provider

class TraktPopularShowsDataSource @Inject constructor(
    private val showService: Provider<Shows>,
    private val retrofitRunner: RetrofitRunner,
    private val showMapper: TraktShowToTiviShow
) : PopularShowsDataSource {
    private val entryMapper = object : IndexedMapper<Show, PopularShowEntry> {
        override suspend fun map(index: Int, from: Show): PopularShowEntry {
            return PopularShowEntry(showId = 0, pageOrder = index, page = 0)
        }
    }

    private val resultsMapper = pairMapperOf(showMapper, entryMapper)

    override suspend fun getPopularShows(page: Int, pageSize: Int): Result<List<Pair<TiviShow, PopularShowEntry>>> {
        return retrofitRunner.executeForResponse(resultsMapper) {
            // We add 1 because Trakt uses a 1-based index whereas we use a 0-based index
            showService.get().popular(page + 1, pageSize, Extended.NOSEASONS).executeWithRetry()
        }
    }
}