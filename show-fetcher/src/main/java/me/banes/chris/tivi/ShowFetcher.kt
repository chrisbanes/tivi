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

package me.banes.chris.tivi

import com.uwetrottmann.trakt5.entities.Show
import kotlinx.coroutines.experimental.rx2.await
import me.banes.chris.tivi.data.entities.TiviShow
import me.banes.chris.tivi.tmdb.TmdbShowFetcher
import me.banes.chris.tivi.trakt.TraktShowFetcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShowFetcher @Inject constructor(
    private val traktShowFetcher: TraktShowFetcher,
    private val tmdbShowFetcher: TmdbShowFetcher
) {
    suspend fun load(traktId: Int, show: Show? = null): TiviShow {
        return traktShowFetcher.loadShow(traktId, show)
                .await()!!
                .also {
                    if (it.needsUpdateFromTmdb()) {
                        refreshFromTmdb(it.tmdbId!!)
                    }
                }
    }

    suspend fun update(traktId: Int) {
        traktShowFetcher.updateShow(traktId)
                .await()
                ?.also {
                    if (it.needsUpdateFromTmdb()) {
                        refreshFromTmdb(it.tmdbId!!)
                    }
                }
    }

    private suspend fun refreshFromTmdb(tmdbId: Int) = tmdbShowFetcher.updateShow(tmdbId).await()
}