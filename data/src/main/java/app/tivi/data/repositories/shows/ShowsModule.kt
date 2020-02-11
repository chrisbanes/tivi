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

package app.tivi.data.repositories.shows

import app.tivi.data.daos.ShowImagesDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.entities.Success
import app.tivi.data.entities.TiviShow
import app.tivi.inject.Tmdb
import app.tivi.inject.Trakt
import com.dropbox.android.external.store4.StoreBuilder
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

@Module(includes = [ShowsModuleBinds::class, ShowStoreModule::class])
class ShowsModule

@Module
internal abstract class ShowsModuleBinds {
    @Binds
    @Trakt
    abstract fun bindTraktShowDataSource(source: TraktShowDataSource): ShowDataSource

    @Binds
    @Tmdb
    abstract fun bindTmdbShowDataSource(source: TmdbShowDataSource): ShowDataSource

    @Binds
    @Tmdb
    abstract fun bindTmdbShowImagesDataSource(source: TmdbShowImagesDataSource): ShowImagesDataSource
}

@Module
class ShowStoreModule {
    private inner class ShowStoreFetcherResponse(val trakt: TiviShow, val tmdb: TiviShow)

    @Provides
    @Singleton
    fun provideShowStore(
        showDao: TiviShowDao,
        lastRequestStore: ShowLastRequestStore,
        @Trakt traktShowDataSource: ShowDataSource,
        @Tmdb tmdbShowDataSource: ShowDataSource
    ): ShowStore {
        return StoreBuilder.fromNonFlow { showId: Long ->
            val localShow = showDao.getShowWithId(showId)
                ?: throw IllegalArgumentException("No show with id $showId in database")

            coroutineScope {
                val traktResult = async {
                    traktShowDataSource.getShow(localShow)
                }
                val tmdbResult = async {
                    tmdbShowDataSource.getShow(localShow)
                }

                // Update our last request timestamp
                if (traktResult is Success<*> && tmdbResult is Success<*>) {
                    lastRequestStore.updateLastRequest(showId)
                }

                ShowStoreFetcherResponse(
                    trakt = traktResult.await().get() ?: TiviShow.EMPTY_SHOW,
                    tmdb = tmdbResult.await().get() ?: TiviShow.EMPTY_SHOW
                )
            }
        }.persister(
            reader = showDao::getShowWithIdFlow,
            writer = { id, response ->
                showDao.withTransaction {
                    val local = showDao.getShowWithId(id) ?: TiviShow.EMPTY_SHOW
                    showDao.insertOrUpdate(mergeShows(local, response.trakt, response.tmdb))
                }
            },
            delete = showDao::delete,
            deleteAll = showDao::deleteAll
        ).build()
    }

    @Provides
    @Singleton
    fun provideTmdbShowImagesStore(
        showImagesDao: ShowImagesDao,
        showDao: TiviShowDao,
        lastRequestStore: ShowImagesLastRequestStore,
        @Tmdb tmdbShowImagesDataSource: ShowImagesDataSource
    ): ShowImagesStore {
        return StoreBuilder.fromNonFlow { showId: Long ->
            val show = showDao.getShowWithId(showId)
                ?: throw IllegalArgumentException("Show with ID $showId does not exist")
            val result = tmdbShowImagesDataSource.getShowImages(show)

            if (result is Success) {
                lastRequestStore.updateLastRequest(showId)
            }

            result.getOrThrow().map {
                it.copy(showId = showId)
            }
        }.persister(
            reader = showImagesDao::getImagesForShowId,
            writer = showImagesDao::saveImages,
            delete = showImagesDao::deleteForShowId,
            deleteAll = showImagesDao::deleteAll
        ).build()
    }
}
