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

package app.tivi.data.watchedshows

import app.tivi.data.daos.TiviShowDao
import app.tivi.data.daos.WatchedShowDao
import app.tivi.data.daos.getIdOrSavePlaceholder
import app.tivi.data.db.DatabaseTransactionRunner
import app.tivi.data.models.WatchedShowEntry
import app.tivi.data.util.syncerForEntity
import app.tivi.inject.ApplicationScope
import app.tivi.util.Logger
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.flow.map
import me.tatarka.inject.annotations.Inject
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreBuilder

@ApplicationScope
@Inject
class WatchedShowsStore(
    dataSource: WatchedShowsDataSource,
    watchedShowsDao: WatchedShowDao,
    showDao: TiviShowDao,
    lastRequestStore: WatchedShowsLastRequestStore,
    logger: Logger,
    transactionRunner: DatabaseTransactionRunner,
) : Store<Unit, List<WatchedShowEntry>> by StoreBuilder.from(
    fetcher = Fetcher.of {
        dataSource()
            .also { lastRequestStore.updateLastRequest() }
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = {
            watchedShowsDao.entriesObservable().map { entries ->
                when {
                    // Store only treats null as 'no value', so convert to null
                    entries.isEmpty() -> null
                    // If the request is expired, our data is stale
                    lastRequestStore.isRequestExpired(6.hours) -> null
                    // Otherwise, our data is fresh and valid
                    else -> entries
                }
            }
        },
        writer = { _: Unit, response ->
            val syncer = syncerForEntity(
                entityDao = watchedShowsDao,
                entityToKey = { it.showId },
                mapper = { newEntity, currentEntity ->
                    newEntity.copy(id = currentEntity?.id ?: 0)
                },
                logger = logger,
            )
            transactionRunner {
                syncer.sync(
                    currentValues = watchedShowsDao.entries(),
                    networkValues = response.map { (show, entry) ->
                        entry.copy(showId = showDao.getIdOrSavePlaceholder(show))
                    },
                )
            }
        },
        delete = {
            // Delete of an entity here means the entire list
            watchedShowsDao.deleteAll()
        },
        deleteAll = watchedShowsDao::deleteAll,
    ),
).build()
