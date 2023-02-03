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

package app.tivi.data.shows

import app.tivi.data.daos.TiviShowDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object ShowsModule {
    @Provides
    @Singleton
    fun provideShowStore(
        showDao: TiviShowDao,
        lastRequestStore: ShowLastRequestStore,
        traktShowDataSource: TraktShowDataSource,
        tmdbShowDataSource: TmdbShowDataSource,
    ): ShowStore = ShowStore(
        showDao = showDao,
        lastRequestStore = lastRequestStore,
        traktShowDataSource = traktShowDataSource,
        tmdbShowDataSource = tmdbShowDataSource,
    )
}
