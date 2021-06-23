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

package app.tivi.data.repositories.showimages

import app.tivi.data.daos.ShowTmdbImagesDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.entities.ShowTmdbImage
import app.tivi.data.entities.Success
import app.tivi.inject.Tmdb
import com.dropbox.android.external.store4.Fetcher
import com.dropbox.android.external.store4.SourceOfTruth
import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreBuilder
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.map
import org.threeten.bp.Duration
import javax.inject.Singleton

typealias ShowImagesStore = Store<Long, List<ShowTmdbImage>>

@InstallIn(SingletonComponent::class)
@Module
internal abstract class ShowDataSourceBinds {
    @Binds
    @Tmdb
    abstract fun bindTmdbShowImagesDataSource(source: TmdbShowImagesDataSource): ShowImagesDataSource
}

@InstallIn(SingletonComponent::class)
@Module
object ShowImagesStoreModule {
    @Provides
    @Singleton
    fun provideTmdbShowImagesStore(
        showTmdbImagesDao: ShowTmdbImagesDao,
        showDao: TiviShowDao,
        lastRequestStore: ShowImagesLastRequestStore,
        @Tmdb tmdbShowImagesDataSource: ShowImagesDataSource
    ): ShowImagesStore = StoreBuilder.from(
        fetcher = Fetcher.of { showId: Long ->
            val show = showDao.getShowWithId(showId)
                ?: throw IllegalArgumentException("Show with ID $showId does not exist")
            val result = tmdbShowImagesDataSource.getShowImages(show)

            if (result is Success) {
                lastRequestStore.updateLastRequest(showId)
            }

            result.getOrThrow().map {
                it.copy(showId = showId)
            }
        },
        sourceOfTruth = SourceOfTruth.of(
            reader = { showId ->
                showTmdbImagesDao.getImagesForShowId(showId).map { entries ->
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
            writer = showTmdbImagesDao::saveImages,
            delete = showTmdbImagesDao::deleteForShowId,
            deleteAll = showTmdbImagesDao::deleteAll
        )
    ).build()
}
