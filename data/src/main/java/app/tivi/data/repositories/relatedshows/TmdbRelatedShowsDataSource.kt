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
import app.tivi.data.mappers.ShowIdToTmdbIdMapper
import app.tivi.data.mappers.TmdbBaseShowToTiviShow
import app.tivi.data.mappers.TmdbShowResultsPageUnwrapper
import app.tivi.data.mappers.pairMapperOf
import app.tivi.extensions.executeWithRetry
import com.uwetrottmann.tmdb2.Tmdb
import com.uwetrottmann.tmdb2.entities.BaseTvShow
import javax.inject.Inject

class TmdbRelatedShowsDataSource @Inject constructor(
    private val tmdbIdMapper: ShowIdToTmdbIdMapper,
    private val retrofitRunner: RetrofitRunner,
    private val tmdb: Tmdb,
    private val showMapper: TmdbBaseShowToTiviShow
) : RelatedShowsDataSource {
    private val entryMapper = object : IndexedMapper<BaseTvShow, RelatedShowEntry> {
        override suspend fun map(index: Int, from: BaseTvShow): RelatedShowEntry {
            return RelatedShowEntry(showId = 0, otherShowId = 0, orderIndex = index)
        }
    }

    private val resultMapper = TmdbShowResultsPageUnwrapper(pairMapperOf(showMapper, entryMapper))

    override suspend fun getRelatedShows(showId: Long): Result<List<Pair<TiviShow, RelatedShowEntry>>> {
        return retrofitRunner.executeForResponse(resultMapper) {
            tmdb.tvService().similar(tmdbIdMapper.map(showId), 1, null).executeWithRetry()
        }
    }
}