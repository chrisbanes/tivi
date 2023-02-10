/*
 * Copyright 2023 Google LLC
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

package app.tivi.data.relatedshows

import app.tivi.data.mappers.IndexedMapper
import app.tivi.data.mappers.ShowIdToTmdbIdMapper
import app.tivi.data.mappers.TmdbBaseShowToTiviShow
import app.tivi.data.mappers.pairMapperOf
import app.tivi.data.mappers.unwrapTmdbShowResults
import app.tivi.data.models.RelatedShowEntry
import app.tivi.data.models.TiviShow
import app.tivi.data.util.bodyOrThrow
import app.tivi.data.util.withRetry
import com.uwetrottmann.tmdb2.Tmdb
import com.uwetrottmann.tmdb2.entities.BaseTvShow
import javax.inject.Inject
import retrofit2.awaitResponse

class TmdbRelatedShowsDataSourceImpl @Inject constructor(
    private val tmdbIdMapper: ShowIdToTmdbIdMapper,
    private val tmdb: Tmdb,
    showMapper: TmdbBaseShowToTiviShow,
) : RelatedShowsDataSource {
    private val entryMapper = IndexedMapper<BaseTvShow, RelatedShowEntry> { index, _ ->
        RelatedShowEntry(showId = 0, otherShowId = 0, orderIndex = index)
    }
    private val resultMapper = unwrapTmdbShowResults(pairMapperOf(showMapper, entryMapper))

    override suspend operator fun invoke(
        showId: Long,
    ): List<Pair<TiviShow, RelatedShowEntry>> = withRetry {
        tmdb.tvService()
            .recommendations(tmdbIdMapper.map(showId), 1, null)
            .awaitResponse()
            .let { resultMapper.invoke(it.bodyOrThrow()) }
    }
}
