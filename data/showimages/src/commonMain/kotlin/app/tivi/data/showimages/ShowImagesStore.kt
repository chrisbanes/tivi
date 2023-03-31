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

package app.tivi.data.showimages

import app.tivi.data.daos.ShowTmdbImagesDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.daos.saveImages
import app.tivi.data.db.DatabaseTransactionRunner
import app.tivi.data.models.ShowTmdbImage
import app.tivi.inject.ApplicationScope
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.flow.map
import me.tatarka.inject.annotations.Inject
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreBuilder

@ApplicationScope
@Inject
class ShowImagesStore(
    showTmdbImagesDao: ShowTmdbImagesDao,
    showDao: TiviShowDao,
    lastRequestStore: ShowImagesLastRequestStore,
    dataSource: ShowImagesDataSource,
    transactionRunner: DatabaseTransactionRunner,
) : Store<Long, List<ShowTmdbImage>> by StoreBuilder.from(
    fetcher = Fetcher.of { showId: Long ->
        val show = showDao.getShowWithId(showId)
        if (show?.tmdbId != null) {
            dataSource.getShowImages(show)
                .also { lastRequestStore.updateLastRequest(showId) }
                .map { it.copy(showId = showId) }
        } else {
            emptyList()
        }
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { showId ->
            showTmdbImagesDao.getImagesForShowId(showId).map { entries ->
                when {
                    // Store only treats null as 'no value', so convert to null
                    entries.isEmpty() -> null
                    // If the request is expired, our data is stale
                    lastRequestStore.isRequestExpired(showId, 180.days) -> null
                    // Otherwise, our data is fresh (enough) and valid
                    else -> entries
                }
            }
        },
        writer = { showId, images ->
            transactionRunner {
                showTmdbImagesDao.saveImages(showId, images)
            }
        },
        delete = showTmdbImagesDao::deleteForShowId,
        deleteAll = showTmdbImagesDao::deleteAll,
    ),
).build()
