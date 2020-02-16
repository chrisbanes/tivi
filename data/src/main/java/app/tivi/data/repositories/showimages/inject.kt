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

import androidx.annotation.VisibleForTesting
import app.tivi.data.daos.ShowImagesDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.entities.ShowTmdbImage
import app.tivi.data.entities.Success
import app.tivi.inject.ForStore
import app.tivi.inject.Tmdb
import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreBuilder
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope

@Module(includes = [ShowImagesModuleBinds::class, ShowImagesStoreModule::class])
class ShowsImagesModule

typealias ShowImagesStore = Store<Long, List<ShowTmdbImage>>

@Module
internal abstract class ShowImagesModuleBinds {
    @Binds
    @Tmdb
    abstract fun bindTmdbShowImagesDataSource(source: TmdbShowImagesDataSource): ShowImagesDataSource
}

@Module
@VisibleForTesting
class ShowImagesStoreModule {
    @Provides
    @Singleton
    fun provideTmdbShowImagesStore(
        showImagesDao: ShowImagesDao,
        showDao: TiviShowDao,
        lastRequestStore: ShowImagesLastRequestStore,
        @Tmdb tmdbShowImagesDataSource: ShowImagesDataSource,
        @ForStore scope: CoroutineScope
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
        ).scope(scope).build()
    }
}
