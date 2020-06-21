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

package app.tivi.data.repositories.shows

import app.tivi.data.daos.TiviShowDao
import app.tivi.data.entities.Success
import app.tivi.data.entities.TiviShow
import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Singleton

typealias ShowStore = Store<Long, TiviShow>

@InstallIn(ApplicationComponent::class)
@Module
object ShowStoreModule {
    @Provides
    @Singleton
    fun provideShowStore(
        showDao: TiviShowDao,
        lastRequestStore: ShowLastRequestStore,
        traktShowDataSource: TraktShowDataSource
    ): ShowStore {
        return StoreBuilder.fromNonFlow { showId: Long ->
            val localShow = showDao.getShowWithId(showId)
                ?: throw IllegalArgumentException("No show with id $showId in database")

            coroutineScope {
                val traktResult = async {
                    traktShowDataSource.getShow(localShow)
                }

                // Update our last request timestamp
                if (traktResult is Success<*>) {
                    lastRequestStore.updateLastRequest(showId)
                }

                traktResult.await().get() ?: TiviShow.EMPTY_SHOW
            }
        }.persister(
            reader = showDao::getShowWithIdFlow,
            writer = { id, response ->
                showDao.withTransaction {
                    val local = showDao.getShowWithId(id) ?: TiviShow.EMPTY_SHOW
                    showDao.insertOrUpdate(mergeShows(local = local, trakt = response))
                }
            },
            delete = showDao::delete,
            deleteAll = showDao::deleteAll
        ).build()
    }
}
