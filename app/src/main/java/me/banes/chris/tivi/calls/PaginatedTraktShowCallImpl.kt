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
import io.reactivex.Maybe
import io.reactivex.Single
import me.banes.chris.tivi.data.Page
import me.banes.chris.tivi.data.TiviShow
import me.banes.chris.tivi.data.TiviShowDao
import me.banes.chris.tivi.util.AppRxSchedulers
import me.banes.chris.tivi.util.DatabaseTxRunner
import timber.log.Timber

abstract class PaginatedTraktShowCallImpl<RS>(
        private val databaseTxRunner: DatabaseTxRunner,
        protected val showDao: TiviShowDao,
        protected val trakt: TraktV2,
        protected val schedulers: AppRxSchedulers,
        protected val tmdbShowFetcher: TmdbShowFetcher,
        protected var pageSize: Int = DEFAULT_PAGE_SIZE
) : PaginatedCall<Unit, List<TiviShow>> {

    companion object {
        val DEFAULT_PAGE_SIZE = 15
    }

    private fun loadPage(page: Int = 0, resetOnSave: Boolean = false): Single<List<TiviShow>> {
        return networkCall(page)
                .subscribeOn(schedulers.network)
                .toFlowable()
                .flatMapIterable { it }
                .filter { filterResponse(it) }
                .flatMap { loadShow(it).toFlowable() }
                .toList()
                .observeOn(schedulers.disk)
                .doOnSuccess { savePage(Page(page, it), resetOnSave) }
    }

    protected abstract fun networkCall(page: Int): Single<List<RS>>

    protected abstract fun filterResponse(response: RS): Boolean

    protected abstract fun lastPageLoaded(): Single<Int>

    override fun refresh(param: Unit): Completable {
        return loadPage(0, resetOnSave = true).toCompletable()
    }

    override fun loadNextPage(): Completable {
        return lastPageLoaded()
                .subscribeOn(schedulers.disk)
                .flatMap { loadPage(it + 1) }
                .toCompletable()
    }

    protected abstract fun deleteEntries()

    protected abstract fun deletePage(page: Int)

    private fun savePage(page: Page<TiviShow>, resetOnSave: Boolean) {
        databaseTxRunner.runInTransaction {
            if (resetOnSave) deleteEntries() else deletePage(page.page)
            page.items.forEachIndexed { index, show ->
                Timber.d("Saving entry: %s", show)
                saveEntry(show, page.page, index)
            }
        }
    }

    protected abstract fun saveEntry(show: TiviShow, page: Int, order: Int)

    protected abstract fun loadShow(response: RS): Maybe<TiviShow>

}
