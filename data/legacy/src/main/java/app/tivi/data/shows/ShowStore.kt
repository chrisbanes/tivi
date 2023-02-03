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
import app.tivi.data.entities.TiviShow
import kotlinx.coroutines.flow.map
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreBuilder
import org.threeten.bp.Duration

typealias ShowStore = Store<Long, TiviShow>

fun ShowStore(
    showDao: TiviShowDao,
    lastRequestStore: ShowLastRequestStore,
    traktShowDataSource: TraktShowDataSource,
    tmdbShowDataSource: TmdbShowDataSource,
): ShowStore = StoreBuilder.from(
    fetcher = Fetcher.of { id: Long ->
        val savedShow = showDao.getShowWithIdOrThrow(id)

        val traktResult = runCatching { traktShowDataSource.getShow(savedShow) }
        if (traktResult.isSuccess) {
            lastRequestStore.updateLastRequest(id)
            return@of traktResult.getOrThrow()
        }

        // If trakt fails, try TMDb
        val tmdbResult = runCatching { tmdbShowDataSource.getShow(savedShow) }
        if (tmdbResult.isSuccess) {
            lastRequestStore.updateLastRequest(id)
            return@of tmdbResult.getOrThrow()
        }

        throw traktResult.exceptionOrNull()!!
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { showId ->
            showDao.getShowWithIdFlow(showId).map {
                when {
                    // If the request is expired, our data is stale
                    lastRequestStore.isRequestExpired(showId, Duration.ofDays(14)) -> null
                    // Otherwise, our data is fresh and valid
                    else -> it
                }
            }
        },
        writer = { id, response ->
            showDao.withTransaction {
                showDao.insertOrUpdate(
                    mergeShows(local = showDao.getShowWithIdOrThrow(id), trakt = response),
                )
            }
        },
        delete = showDao::delete,
        deleteAll = showDao::deleteAll,
    ),
).build()
