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

import app.moviebase.trakt.TraktExtended
import app.moviebase.trakt.api.TraktShowsApi
import app.moviebase.trakt.model.TraktShow
import app.tivi.data.mappers.IndexedMapper
import app.tivi.data.mappers.ShowIdToTraktIdMapper
import app.tivi.data.mappers.TraktShowToTiviShow
import app.tivi.data.mappers.pairMapperOf
import app.tivi.data.models.RelatedShowEntry
import app.tivi.data.models.TiviShow
import me.tatarka.inject.annotations.Inject

@Inject
class TraktRelatedShowsDataSourceImpl(
    private val traktIdMapper: ShowIdToTraktIdMapper,
    private val showService: Lazy<TraktShowsApi>,
    showMapper: TraktShowToTiviShow,
) : TraktRelatedShowsDataSource {
    private val entryMapper = IndexedMapper<TraktShow, RelatedShowEntry> { index, _ ->
        RelatedShowEntry(showId = 0, otherShowId = 0, orderIndex = index)
    }
    private val resultMapper = pairMapperOf(showMapper, entryMapper)

    override suspend operator fun invoke(showId: Long): List<Pair<TiviShow, RelatedShowEntry>> {
        val traktShowId = traktIdMapper.map(showId)
            ?: throw IllegalArgumentException("No Trakt ID for show with ID: $showId")

        return showService.value
            .getRelated(traktShowId.toString(), 0, 10, TraktExtended.NO_SEASONS)
            .let { resultMapper(it) }
    }
}
