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
import com.uwetrottmann.trakt5.entities.TrendingShow
import com.uwetrottmann.trakt5.enums.Extended
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import me.banes.chris.tivi.data.TiviShow
import me.banes.chris.tivi.data.TiviShowDao
import me.banes.chris.tivi.data.TrendingDao
import me.banes.chris.tivi.data.TrendingEntry
import me.banes.chris.tivi.extensions.toRxSingle
import me.banes.chris.tivi.util.AppRxSchedulers
import me.banes.chris.tivi.util.DatabaseTxRunner
import javax.inject.Inject

class TrendingCall @Inject constructor(
        databaseTxRunner: DatabaseTxRunner,
        showDao: TiviShowDao,
        private val trendingDao: TrendingDao,
        tmdbShowFetcher: TmdbShowFetcher,
        trakt: TraktV2,
        schedulers: AppRxSchedulers)
    : PaginatedTraktShowCallImpl<TrendingShow>(databaseTxRunner, showDao, trakt, schedulers, tmdbShowFetcher) {

    override fun data(): Flowable<List<TiviShow>> {
        return trendingDao.entries()
                .subscribeOn(schedulers.disk)
                .distinctUntilChanged()
    }

    override fun data(page: Int): Flowable<List<TiviShow>> {
        return trendingDao.entriesPage(page)
                .subscribeOn(schedulers.disk)
                .distinctUntilChanged()
    }

    override fun networkCall(page: Int): Single<List<TrendingShow>> {
        return trakt.shows()
                .trending(page + 1, DEFAULT_PAGE_SIZE, Extended.NOSEASONS)
                .toRxSingle()
    }

    override fun filterResponse(response: TrendingShow): Boolean {
        return response.show.ids.tmdb != null
    }

    override fun loadShow(response: TrendingShow): Maybe<TiviShow> {
        return tmdbShowFetcher.showFromTmdb(response.show.ids.tmdb, response.show.ids.trakt)
    }

    override fun lastPageLoaded(): Single<Int> {
        return trendingDao.getLastPage()
    }

    override fun deleteEntries() {
        trendingDao.deleteAll()
    }

    override fun deletePage(page: Int) {
        trendingDao.deletePage(page)
    }

    override fun saveEntry(show: TiviShow, page: Int, order: Int) {
        assert(show.id != null)
        val entry = TrendingEntry(showId = show.id!!, page = page, pageOrder = order)
        trendingDao.insert(entry)
    }

}