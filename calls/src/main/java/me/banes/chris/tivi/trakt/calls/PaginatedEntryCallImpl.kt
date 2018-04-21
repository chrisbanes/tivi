/*
 * Copyright 2017 Google, Inc.
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

package me.banes.chris.tivi.trakt.calls

import android.arch.paging.DataSource
import io.reactivex.Flowable
import io.reactivex.Single
import kotlinx.coroutines.experimental.rx2.await
import me.banes.chris.tivi.calls.PaginatedCall
import me.banes.chris.tivi.data.DatabaseTransactionRunner
import me.banes.chris.tivi.data.PaginatedEntry
import me.banes.chris.tivi.data.daos.PaginatedEntryDao
import me.banes.chris.tivi.data.daos.TiviShowDao
import me.banes.chris.tivi.data.entities.ListItem
import me.banes.chris.tivi.data.entities.TiviShow
import me.banes.chris.tivi.util.AppRxSchedulers
import timber.log.Timber

abstract class PaginatedEntryCallImpl<TT, ET : PaginatedEntry, LI : ListItem<ET>, out ED : PaginatedEntryDao<ET, LI>>(
    private val databaseTransactionRunner: DatabaseTransactionRunner,
    protected val showDao: TiviShowDao,
    private val entryDao: ED,
    protected val schedulers: AppRxSchedulers,
    override val pageSize: Int = 21
) : PaginatedCall<Unit, LI> {

    override fun data(param: Unit): Flowable<List<LI>> {
        return entryDao.entries()
                .distinctUntilChanged()
                .subscribeOn(schedulers.database)
    }

    override fun data(page: Int): Flowable<List<LI>> {
        return entryDao.entriesPage(page)
                .subscribeOn(schedulers.database)
                .distinctUntilChanged()
    }

    override fun dataSourceFactory(): DataSource.Factory<Int, LI> = entryDao.entriesDataSource()

    private fun loadPage(page: Int = 0, resetOnSave: Boolean = false): Single<List<ET>> {
        return networkCall(page)
                .subscribeOn(schedulers.network)
                .toFlowable()
                .flatMapIterable { it }
                .flatMapSingle { traktObject ->
                    loadShow(traktObject).map { show -> mapToEntry(traktObject, show, page) }
                }
                .toList()
                .observeOn(schedulers.database)
                .doOnSuccess { savePage(it, page, resetOnSave) }
    }

    override suspend fun refresh(param: Unit) {
        loadPage(0, resetOnSave = true).await()
    }

    override suspend fun loadNextPage() {
        entryDao.getLastPage()
                .subscribeOn(schedulers.database)
                .flatMap { loadPage(it + 1) }
                .await()
    }

    private fun savePage(items: List<ET>, page: Int, resetOnSave: Boolean) {
        databaseTransactionRunner.runInTransaction {
            when {
                resetOnSave -> entryDao.deleteAll()
                else -> entryDao.deletePage(page)
            }
            items.forEach { show ->
                Timber.d("Saving entry: %s", show)
                entryDao.insert(show)
            }
        }
    }

    protected abstract fun mapToEntry(networkEntity: TT, show: TiviShow, page: Int): ET

    protected abstract fun loadShow(response: TT): Single<TiviShow>

    protected abstract fun networkCall(page: Int): Single<List<TT>>
}
