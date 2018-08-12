/*
 * Copyright 2018 Google, Inc.
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

import app.tivi.data.entities.RelatedShowEntry
import app.tivi.data.entities.TiviShow
import app.tivi.data.mappers.ShowIdToTmdbIdMapper
import app.tivi.data.mappers.TmdbBaseShowToTiviShow
import app.tivi.extensions.fetchBody
import com.uwetrottmann.tmdb2.Tmdb
import javax.inject.Inject

class TmdbRelatedShowsDataSource @Inject constructor(
    private val tmdbIdMapper: ShowIdToTmdbIdMapper,
    private val tmdb: Tmdb,
    private val mapper: TmdbBaseShowToTiviShow
) : RelatedShowsDataSource {
    override suspend fun getRelatedShows(showId: Long): List<Pair<TiviShow, RelatedShowEntry>> {
        val traktId = tmdbIdMapper.map(showId) ?: return emptyList()

        val results = tmdb.tvService().similar(traktId, 1, null).fetchBody()

        return results.results.mapIndexed { index, relatedShow ->
            mapper.map(relatedShow) to RelatedShowEntry(showId = showId, otherShowId = 0, orderIndex = index)
        }
    }
}