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
import app.tivi.inject.ForStore
import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

typealias RelatedShowsStore = Store<Long, List<RelatedShowEntry>>

@InstallIn(ApplicationComponent::class)
@Module
internal object RelatedShowsModule {
    @Provides
    @Singleton
    fun provideRelatedShowsStore(
        tmdbRelatedShows: TmdbRelatedShowsDataSource,
        relatedShowsDao: RelatedShowsDao,
        showDao: TiviShowDao,
        lastRequestStore: RelatedShowsLastRequestStore,
        @ForStore scope: CoroutineScope
    ): RelatedShowsStore {
        return StoreBuilder.fromNonFlow { showId: Long ->
            val response = tmdbRelatedShows(showId)
            if (response is Success) {
                lastRequestStore.updateLastRequest(showId)
            }
            response.getOrThrow()
        }.persister(
            reader = relatedShowsDao::entriesObservable,
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
        ).scope(scope).build()
    }
}
