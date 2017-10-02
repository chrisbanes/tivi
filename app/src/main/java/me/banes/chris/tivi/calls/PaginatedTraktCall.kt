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

import com.uwetrottmann.tmdb2.Tmdb
import com.uwetrottmann.tmdb2.entities.TvShow
import com.uwetrottmann.trakt5.TraktV2
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import me.banes.chris.tivi.data.Page
import me.banes.chris.tivi.data.TiviShow
import me.banes.chris.tivi.data.TiviShowDao
import me.banes.chris.tivi.extensions.toRxSingle
import me.banes.chris.tivi.util.AppRxSchedulers
import me.banes.chris.tivi.util.DatabaseTxRunner
import timber.log.Timber
import java.util.Date

abstract class PaginatedTraktCall<RS>(
        val databaseTxRunner: DatabaseTxRunner,
        val showDao: TiviShowDao,
        val tmdb: Tmdb,
        val trakt: TraktV2,
        val schedulers: AppRxSchedulers,
        var pageSize: Int = DEFAULT_PAGE_SIZE) {

    companion object {
        val DEFAULT_PAGE_SIZE = 15
    }

    fun data(page: Int? = null): Flowable<List<TiviShow>> {
        return createData(page)
                .subscribeOn(schedulers.disk)
                .distinctUntilChanged()
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

    protected abstract fun createData(page: Int? = null): Flowable<List<TiviShow>>

    fun refresh(): Completable {
        return loadPage(0, resetOnSave = true).toCompletable()
    }

    fun loadNextPage(): Completable {
        return lastPageLoaded()
                .subscribeOn(schedulers.disk)
                .flatMap { loadPage(it + 1) }
                .toCompletable()
    }

    protected abstract fun deleteEntries()

    protected abstract fun deletePage(page: Int)

    protected fun savePage(page: Page<TiviShow>, resetOnSave: Boolean) {
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

    protected fun showFromTmdb(tmdbId: Int, traktId: Int): Maybe<TiviShow> {
        val dbSource = showDao.getShowFromId(tmdbId, traktId)
                .subscribeOn(schedulers.disk)
                .filter { !it.needsUpdateFromTmdb() } // Don't emit if the item needs updating

        val networkSource = tmdb.tvService().tv(tmdbId).toRxSingle()
                .subscribeOn(schedulers.network)
                .observeOn(schedulers.disk)
                .flatMap { mapTmdbShow(it) }
                .map {
                    var show = it
                    if (show.traktId == null) {
                        show = show.copy(traktId = traktId)
                    }
                    if (it.id == null) {
                        Timber.d("Inserting show: %s", show)
                        show = show.copy(id = showDao.insertShow(show))
                    } else {
                        Timber.d("Updating show: %s", show)
                        showDao.updateShow(show)
                    }
                    show
                }

        return Maybe.concat(dbSource, networkSource.toMaybe()).firstElement()
    }

    private fun mapTmdbShow(tmdbShow: TvShow): Single<TiviShow> {
        return showDao.getShowWithTmdbId(tmdbShow.id)
                .subscribeOn(schedulers.disk)
                .defaultIfEmpty(TiviShow(title = tmdbShow.name))
                .map {
                    it.copy(
                        title = tmdbShow.name,
                        tmdbId = tmdbShow.id,
                        summary = tmdbShow.overview,
                        tmdbBackdropPath = tmdbShow.backdrop_path,
                        tmdbPosterPath = tmdbShow.poster_path,
                        homepage = tmdbShow.homepage,
                        originalTitle = tmdbShow.original_name,
                        lastTmdbUpdate = Date()
                    )
                }.toSingle()
    }

}
