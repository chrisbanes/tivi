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

package app.tivi.data.repositories.trendingshows

import app.tivi.data.RetrofitRunner
import app.tivi.data.entities.Result
import app.tivi.data.entities.TiviShow
import app.tivi.data.entities.TrendingShowEntry
import app.tivi.data.mappers.Mapper
import app.tivi.data.mappers.TraktTrendingShowToTiviShow
import app.tivi.data.mappers.pairMapperOf
import app.tivi.extensions.executeWithRetry
import com.uwetrottmann.trakt5.entities.TrendingShow
import com.uwetrottmann.trakt5.enums.Extended
import com.uwetrottmann.trakt5.services.Shows
import javax.inject.Inject
import javax.inject.Provider

class TraktTrendingShowsDataSource @Inject constructor(
    private val showService: Provider<Shows>,
    private val retrofitRunner: RetrofitRunner,
    private val showMapper: TraktTrendingShowToTiviShow
) : TrendingShowsDataSource {
    private val entryMapper = object : Mapper<TrendingShow, TrendingShowEntry> {
        override suspend fun map(from: TrendingShow): TrendingShowEntry {
            return TrendingShowEntry(showId = 0, watchers = from.watchers, page = 0)
        }
    }
    private val responseMapper = pairMapperOf(showMapper, entryMapper)

    override suspend fun getTrendingShows(page: Int, pageSize: Int): Result<List<Pair<TiviShow, TrendingShowEntry>>> {
        return retrofitRunner.executeForResponse(responseMapper) {
            // We add 1 because Trakt uses a 1-based index whereas we use a 0-based index
            showService.get().trending(page + 1, pageSize, Extended.NOSEASONS).executeWithRetry()
        }
    }
}