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

package app.tivi.interactors

import app.tivi.ShowFetcher
import app.tivi.data.DatabaseTransactionRunner
import app.tivi.data.PaginatedEntry
import app.tivi.data.daos.LastRequestDao
import app.tivi.data.daos.PaginatedEntryDao
import app.tivi.data.entities.Request
import app.tivi.data.resultentities.EntryWithShow
import app.tivi.extensions.parallelForEach
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.Logger

class PagedInteractorHelper<TT, ET : PaginatedEntry, LI : EntryWithShow<ET>, out ED : PaginatedEntryDao<ET, LI>>(
    private val databaseTransactionRunner: DatabaseTransactionRunner,
    private val entryDao: ED,
    private val lastRequests: LastRequestDao,
    private val showFetcher: ShowFetcher,
    private val dispatchers: AppCoroutineDispatchers,
    private val logger: Logger,
    private val mapToEntry: suspend (TT, Long, Int) -> ET,
    private val insertShowPlaceholder: suspend (TT) -> Long,
    private val networkCall: suspend (Int) -> List<TT>
) {
    suspend fun loadPage(page: Int = 0, resetOnSave: Boolean = false) {
        networkCall(page)
                .map { mapToEntry(it, insertShowPlaceholder(it), page) }
                .also { savePage(it, page, resetOnSave) }
                .parallelForEach(dispatchers.io) {
                    // Now trigger a refresh of each show if it hasn't been refreshed before
                    if (lastRequests.hasNotBeenRequested(Request.SHOW_DETAILS, it.showId)) {
                        showFetcher.updateIfNeeded(it.showId)
                    }
                }
    }

    private fun savePage(items: List<ET>, page: Int, resetOnSave: Boolean) {
        databaseTransactionRunner.runInTransaction {
            when {
                resetOnSave -> entryDao.deleteAll()
                else -> entryDao.deletePage(page)
            }
            items.forEach { entry ->
                logger.d("Saving entry: %s", entry)
                try {
                    entryDao.insert(entry)
                } catch (e: RuntimeException) {
                    logger.d(e, "Ignoring exception while inserting %s", entry)
                }
            }
        }
    }
}