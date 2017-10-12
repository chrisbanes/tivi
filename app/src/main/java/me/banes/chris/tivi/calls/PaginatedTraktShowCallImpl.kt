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

package me.banes.chris.tivi.calls

import com.uwetrottmann.trakt5.TraktV2
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import me.banes.chris.tivi.data.Page
import me.banes.chris.tivi.data.PaginatedEntry
import me.banes.chris.tivi.data.daos.PaginatedEntryDao
import me.banes.chris.tivi.data.entities.TiviShow
import me.banes.chris.tivi.data.daos.TiviShowDao
import me.banes.chris.tivi.util.AppRxSchedulers
import me.banes.chris.tivi.util.DatabaseTxRunner
import timber.log.Timber

abstract class PaginatedTraktShowCallImpl<TT, ET : PaginatedEntry, out ED : PaginatedEntryDao<ET>>(
        private val databaseTxRunner: DatabaseTxRunner,
        protected val showDao: TiviShowDao,
        protected val entryDao: ED,
        protected val trakt: TraktV2,
        protected val schedulers: AppRxSchedulers,
        protected val tmdbShowFetcher: TmdbShowFetcher,
        protected var pageSize: Int = 15
) : PaginatedCall<Unit, List<ET>> {

    override fun data(): Flowable<List<ET>> {
        return entryDao.entries()
                .subscribeOn(schedulers.disk)
                .distinctUntilChanged()
    }

    override fun data(page: Int): Flowable<List<ET>> {
        return entryDao.entriesPage(page)
                .subscribeOn(schedulers.disk)
                .distinctUntilChanged()
    }

    private fun loadPage(page: Int = 0, resetOnSave: Boolean = false): Single<List<ET>> {
        return networkCall(page)
                .subscribeOn(schedulers.network)
                .toFlowable()
                .flatMapIterable { it }
                .filter { filterResponse(it) }
                .flatMap { traktObject ->
                    loadShow(traktObject)
                            .map { tiviShow ->
                                mapToEntry(traktObject, tiviShow, page, 0)
                            }
                            .toFlowable()
                }
                .toList()
                .observeOn(schedulers.disk)
                .doOnSuccess { savePage(Page(page, it), resetOnSave) }
    }

    override fun refresh(param: Unit): Completable {
        return loadPage(0, resetOnSave = true).toCompletable()
    }

    override fun loadNextPage(): Completable {
        return entryDao.getLastPage()
                .subscribeOn(schedulers.disk)
                .flatMap { loadPage(it + 1) }
                .toCompletable()
    }

    private fun savePage(page: Page<ET>, resetOnSave: Boolean) {
        databaseTxRunner.runInTransaction {
            if (resetOnSave) {
                entryDao.deleteAll()
            } else {
                entryDao.deletePage(page.page)
            }
            page.items.forEach { show ->
                Timber.d("Saving entry: %s", show)
                entryDao.insert(show)
            }
        }
    }

    protected abstract fun mapToEntry(networkEntity: TT, show: TiviShow, page: Int, pageOrder: Int): ET

    protected abstract fun loadShow(response: TT): Maybe<TiviShow>

    protected abstract fun networkCall(page: Int): Single<List<TT>>

    protected abstract fun filterResponse(response: TT): Boolean

}
