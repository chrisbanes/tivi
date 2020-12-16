/*
 * Copyright 2020 Google LLC
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

import app.tivi.data.daos.TiviShowDao
import app.tivi.data.daos.TrendingDao
import app.tivi.data.entities.Success
import app.tivi.data.entities.TrendingShowEntry
import com.dropbox.android.external.store4.Fetcher
import com.dropbox.android.external.store4.SourceOfTruth
import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.map
import org.threeten.bp.Duration
import javax.inject.Singleton

typealias TrendingShowsStore = Store<Int, List<TrendingShowEntry>>

@InstallIn(SingletonComponent::class)
@Module
object TrendingShowsModule {
    @Provides
    @Singleton
    fun provideTrendingShowsStore(
        traktTrendingShows: TraktTrendingShowsDataSource,
        trendingShowsDao: TrendingDao,
        showDao: TiviShowDao,
        lastRequestStore: TrendingShowsLastRequestStore
    ): TrendingShowsStore = StoreBuilder.from(
        fetcher = Fetcher.of { page: Int ->
            traktTrendingShows(page, 20)
                .also {
                    if (page == 0 && it is Success) {
                        lastRequestStore.updateLastRequest()
                    }
                }.getOrThrow()
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { page ->
                trendingShowsDao.entriesObservable(page).map { entries ->
                    when {
                        // Store only treats null as 'no value', so convert to null
                        entries.isEmpty() -> null
                        // If the request is expired, our data is stale
                        lastRequestStore.isRequestExpired(Duration.ofHours(3)) -> null
                        // Otherwise, our data is fresh and valid
                        else -> entries
                    }
                }
            },
            writer = { page, response ->
                trendingShowsDao.withTransaction {
                    val entries = response.map { (show, entry) ->
                        entry.copy(showId = showDao.getIdOrSavePlaceholder(show), page = page)
                    }
                    if (page == 0) {
                        // If we've requested page 0, remove any existing entries first
                        trendingShowsDao.deleteAll()
                        trendingShowsDao.insertAll(entries)
                    } else {
                        trendingShowsDao.updatePage(page, entries)
                    }
                }
            },
            delete = trendingShowsDao::deletePage,
            deleteAll = trendingShowsDao::deleteAll
        )
    ).build()
}
