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

package app.tivi.data.popularshows

import app.moviebase.trakt.TraktExtended
import app.moviebase.trakt.api.TraktShowsApi
import app.moviebase.trakt.model.TraktShow
import app.tivi.data.mappers.IndexedMapper
import app.tivi.data.mappers.TraktShowToTiviShow
import app.tivi.data.mappers.pairMapperOf
import app.tivi.data.models.PopularShowEntry
import app.tivi.data.models.TiviShow
import me.tatarka.inject.annotations.Inject

@Inject
class TraktPopularShowsDataSource(
    private val showService: Lazy<TraktShowsApi>,
    showMapper: TraktShowToTiviShow,
) : PopularShowsDataSource {
    private val entryMapper = IndexedMapper<TraktShow, PopularShowEntry> { index, _ ->
        PopularShowEntry(showId = 0, pageOrder = index, page = 0)
    }

    private val resultsMapper = pairMapperOf(showMapper, entryMapper)

    override suspend operator fun invoke(
        page: Int,
        pageSize: Int,
    ): List<Pair<TiviShow, PopularShowEntry>> =
        showService.value
            .getPopular(page = page + 1, limit = pageSize, extended = TraktExtended.NO_SEASONS)
            .let { resultsMapper(it) }
}
