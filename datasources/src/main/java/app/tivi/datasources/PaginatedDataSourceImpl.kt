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

package app.tivi.datasources

import android.arch.paging.DataSource
import app.tivi.ShowFetcher
import app.tivi.data.DatabaseTransactionRunner
import app.tivi.data.PaginatedEntry
import app.tivi.data.daos.PaginatedEntryDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.entities.ListItem
import app.tivi.extensions.parallelForEach
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.AppRxSchedulers
import app.tivi.util.Logger
import io.reactivex.Flowable
import kotlinx.coroutines.experimental.withContext

abstract class PaginatedDataSourceImpl<TT, ET : PaginatedEntry, LI : ListItem<ET>, out ED : PaginatedEntryDao<ET, LI>>(
    private val databaseTransactionRunner: DatabaseTransactionRunner,
    protected val showDao: TiviShowDao,
    private val entryDao: ED,
    private val showFetcher: ShowFetcher,
    protected val schedulers: AppRxSchedulers,
    private val dispatchers: AppCoroutineDispatchers,
    private val logger: Logger,
    override val pageSize: Int = 21
) : PaginatedDataSource<Unit, LI> {

    override fun data(param: Unit): Flowable<List<LI>> {
        return entryDao.entries()
                .subscribeOn(schedulers.io)
                .distinctUntilChanged()
    }

    override fun data(page: Int): Flowable<List<LI>> {
        return entryDao.entriesPage(page)
                .subscribeOn(schedulers.io)
                .distinctUntilChanged()
    }

    override fun dataSourceFactory(): DataSource.Factory<Int, LI> = entryDao.entriesDataSource()

    private suspend fun loadPage(page: Int = 0, resetOnSave: Boolean = false) {
        withContext(dispatchers.io) {
            networkCall(page)
                    .map { mapToEntry(it, insertShowPlaceholder(it), page) }
                    .also { savePage(it, page, resetOnSave) }
        }.parallelForEach {
            // Now trigger a refresh of each show
            showFetcher.update(it.showId)
        }
    }

    override suspend fun refresh(param: Unit) {
        loadPage(0, resetOnSave = true)
    }

    override suspend fun loadNextPage() {
        withContext(dispatchers.io) {
            loadPage(entryDao.getLastPage() + 1)
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

    protected abstract fun mapToEntry(networkEntity: TT, showId: Long, page: Int): ET

    protected abstract suspend fun insertShowPlaceholder(response: TT): Long

    protected abstract suspend fun networkCall(page: Int): List<TT>
}
