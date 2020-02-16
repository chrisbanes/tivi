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
import com.dropbox.android.external.store4.StoreBuilder
import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope

@Module
class TrendingShowsModule {
    @Provides
    @Singleton
    fun provideTrendingShowsStore(
        traktTrendingShows: TraktTrendingShowsDataSource,
        trendingShowsDao: TrendingDao,
        showDao: TiviShowDao,
        lastRequestStore: TrendingShowsLastRequestStore,
        scope: CoroutineScope
    ): TrendingShowsStore {
        return StoreBuilder.fromNonFlow { page: Int ->
            val response = traktTrendingShows(page, 20)
            if (page == 0 && response is Success) {
                lastRequestStore.updateLastRequest()
            }
            response.getOrThrow()
        }.persister(
            reader = trendingShowsDao::entriesForPage,
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
        ).scope(scope).build()
    }
}
