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

package app.tivi.data.traktauth

import app.moviebase.tmdb.Tmdb3
import app.moviebase.tmdb.model.TmdbShow
import app.tivi.data.mappers.IndexedMapper
import app.tivi.data.mappers.ShowIdToTmdbIdMapper
import app.tivi.data.mappers.TmdbShowToTiviShow
import app.tivi.data.mappers.pairMapperOf
import app.tivi.data.models.RelatedShowEntry
import app.tivi.data.models.TiviShow
import me.tatarka.inject.annotations.Inject

@Inject
class TmdbRelatedShowsDataSourceImpl(
    private val tmdbIdMapper: ShowIdToTmdbIdMapper,
    private val tmdb: Tmdb3,
    showMapper: TmdbShowToTiviShow,
) : TmdbRelatedShowsDataSource {

    private val entryMapper = IndexedMapper<TmdbShow, RelatedShowEntry> { index, _ ->
        RelatedShowEntry(showId = 0, otherShowId = 0, orderIndex = index)
    }
    private val resultMapper = pairMapperOf(showMapper, entryMapper)

    override suspend operator fun invoke(
        showId: Long,
    ): List<Pair<TiviShow, RelatedShowEntry>> {
        val tmdbShowId = tmdbIdMapper.map(showId)
        require(tmdbShowId != null) { "No Tmdb ID for show with ID: $showId" }

        return tmdb.show
            .getRecommendations(tmdbShowId, 1, null)
            .let { resultMapper(it.results) }
    }
}
