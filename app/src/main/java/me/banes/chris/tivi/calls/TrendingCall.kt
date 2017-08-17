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
 *
 */

package me.banes.chris.tivi.calls

import com.uwetrottmann.tmdb2.Tmdb
import com.uwetrottmann.trakt5.TraktV2
import com.uwetrottmann.trakt5.entities.TrendingShow
import com.uwetrottmann.trakt5.enums.Extended
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import me.banes.chris.tivi.data.TiviShow
import me.banes.chris.tivi.data.TiviShowDao
import me.banes.chris.tivi.data.TrendingEntry
import me.banes.chris.tivi.util.AppRxSchedulers
import me.banes.chris.tivi.util.DatabaseTxRunner
import javax.inject.Inject

class TrendingCall @Inject constructor(
        databaseTxRunner: DatabaseTxRunner,
        showDao: TiviShowDao,
        tmdb: Tmdb,
        trakt: TraktV2,
        schedulers: AppRxSchedulers)
    : PaginatedTraktCall<TrendingShow>(databaseTxRunner, showDao, tmdb, trakt, schedulers) {

    override fun createData(): Flowable<List<TiviShow>> {
        return showDao.trendingShows()
    }

    override fun networkCall(page: Int): Single<List<TrendingShow>> {
        return Single.fromCallable {
            trakt.shows().trending(page + 1, DEFAULT_PAGE_SIZE, Extended.NOSEASONS).execute().body()
        }
    }

    override fun filterResponse(response: TrendingShow): Boolean {
        return response.show.ids.tmdb != null
    }

    override fun loadShow(response: TrendingShow): Maybe<TiviShow> {
        return showFromTmdb(response.show.ids.tmdb, response.show.ids.trakt)
    }

    override fun lastPageLoaded(): Single<Int> {
        return showDao.getLastTrendingPage()
    }

    override fun deleteEntries() {
        showDao.deleteTrendingShows()
    }

    override fun deletePage(page: Int) {
        showDao.deleteTrendingShowsPageSync(page)
    }

    override fun saveEntry(show: TiviShow, page: Int, order: Int) {
        val entry = TrendingEntry(showId = show.id, page = page, pageOrder = order)
        entry.id = showDao.insertTrending(entry)
    }

}