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

package app.tivi.data.trendingshows

import app.tivi.data.mappers.TraktTrendingShowToTiviShow
import app.tivi.data.mappers.TraktTrendingShowToTrendingShowEntry
import app.tivi.data.mappers.pairMapperOf
import app.tivi.data.models.TiviShow
import app.tivi.data.models.TrendingShowEntry
import app.tivi.data.util.bodyOrThrow
import app.tivi.data.util.withRetry
import com.uwetrottmann.trakt5.enums.Extended
import com.uwetrottmann.trakt5.services.Shows
import me.tatarka.inject.annotations.Inject
import retrofit2.awaitResponse

@Inject
class TraktTrendingShowsDataSource(
    private val showService: Lazy<Shows>,
    showMapper: TraktTrendingShowToTiviShow,
    entryMapper: TraktTrendingShowToTrendingShowEntry,
) : TrendingShowsDataSource {
    private val responseMapper = pairMapperOf(showMapper, entryMapper)

    override suspend operator fun invoke(
        page: Int,
        pageSize: Int,
    ): List<Pair<TiviShow, TrendingShowEntry>> = withRetry {
        showService.value
            // We add 1 because Trakt uses a 1-based index whereas we use a 0-based index
            .trending(page + 1, pageSize, Extended.NOSEASONS)
            .awaitResponse()
            .let { responseMapper.invoke(it.bodyOrThrow()) }
    }
}
