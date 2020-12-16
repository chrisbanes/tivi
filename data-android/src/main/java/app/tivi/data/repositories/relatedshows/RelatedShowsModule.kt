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

package app.tivi.data.repositories.relatedshows

import app.tivi.data.daos.RelatedShowsDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.entities.RelatedShowEntry
import app.tivi.data.entities.Success
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

typealias RelatedShowsStore = Store<Long, List<RelatedShowEntry>>

@InstallIn(SingletonComponent::class)
@Module
internal object RelatedShowsModule {
    @Provides
    @Singleton
    fun provideRelatedShowsStore(
        tmdbRelatedShows: TmdbRelatedShowsDataSource,
        relatedShowsDao: RelatedShowsDao,
        showDao: TiviShowDao,
        lastRequestStore: RelatedShowsLastRequestStore
    ): RelatedShowsStore = StoreBuilder.from(
        fetcher = Fetcher.of { showId: Long ->
            tmdbRelatedShows(showId)
                .also {
                    if (it is Success) {
                        lastRequestStore.updateLastRequest(showId)
                    }
                }.getOrThrow()
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { showId ->
                relatedShowsDao.entriesObservable(showId).map { entries ->
                    when {
                        // Store only treats null as 'no value', so convert to null
                        entries.isEmpty() -> null
                        // If the request is expired, our data is stale
                        lastRequestStore.isRequestExpired(showId, Duration.ofDays(28)) -> null
                        // Otherwise, our data is fresh and valid
                        else -> entries
                    }
                }
            },
            writer = { showId, response ->
                relatedShowsDao.withTransaction {
                    val entries = response.map { (show, entry) ->
                        entry.copy(
                            showId = showId,
                            otherShowId = showDao.getIdOrSavePlaceholder(show)
                        )
                    }
                    relatedShowsDao.deleteWithShowId(showId)
                    relatedShowsDao.insertOrUpdate(entries)
                }
            },
            delete = relatedShowsDao::deleteWithShowId,
            deleteAll = relatedShowsDao::deleteAll
        )
    ).build()
}
