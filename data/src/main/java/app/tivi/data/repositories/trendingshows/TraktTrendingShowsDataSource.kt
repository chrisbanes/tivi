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

package app.tivi.data.repositories.trendingshows

import app.tivi.data.entities.TiviShow
import app.tivi.data.entities.TrendingShowEntry
import app.tivi.data.mappers.TraktShowToTiviShow
import app.tivi.extensions.fetchBodyWithRetry
import com.uwetrottmann.trakt5.enums.Extended
import com.uwetrottmann.trakt5.services.Shows
import javax.inject.Inject
import javax.inject.Provider

class TraktTrendingShowsDataSource @Inject constructor(
    private val showService: Provider<Shows>,
    private val mapper: TraktShowToTiviShow
) : TrendingShowsDataSource {
    override suspend fun getTrendingShows(page: Int, pageSize: Int): List<Pair<TiviShow, TrendingShowEntry>> {
        // We add 1 because Trakt uses a 1-based index whereas we use a 0-based index
        val results = showService.get().trending(page + 1, pageSize, Extended.NOSEASONS).fetchBodyWithRetry()
        return results.map { show ->
            mapper.map(show.show) to TrendingShowEntry(showId = 0, watchers = show.watchers, page = page)
        }
    }
}