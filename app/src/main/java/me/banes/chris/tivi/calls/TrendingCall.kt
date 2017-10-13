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
import io.reactivex.Maybe
import io.reactivex.Single
import me.banes.chris.tivi.data.daos.TiviShowDao
import me.banes.chris.tivi.data.daos.TrendingDao
import me.banes.chris.tivi.data.entities.TiviShow
import me.banes.chris.tivi.data.entities.TrendingEntry
import me.banes.chris.tivi.extensions.toRxSingle
import me.banes.chris.tivi.util.AppRxSchedulers
import me.banes.chris.tivi.util.DatabaseTxRunner
import javax.inject.Inject

class TrendingCall @Inject constructor(
        databaseTxRunner: DatabaseTxRunner,
        showDao: TiviShowDao,
        trendingDao: TrendingDao,
        traktShowFetcher: TraktShowFetcher,
        trakt: TraktV2,
        schedulers: AppRxSchedulers)
    : PaginatedEntryCallImpl<TrendingShow, TrendingEntry, TrendingDao>(databaseTxRunner, showDao, trendingDao, trakt, schedulers, traktShowFetcher) {

    override fun networkCall(page: Int): Single<List<TrendingShow>> {
        return trakt.shows()
                .trending(page + 1, page, Extended.NOSEASONS)
                .toRxSingle()
    }

    override fun filterResponse(response: TrendingShow): Boolean {
        return response.show.ids.tmdb != null
    }

    override fun loadShow(response: TrendingShow): Maybe<TiviShow> {
        return traktShowFetcher.getShow(response.show.ids.trakt, response.show)
    }

    override fun mapToEntry(networkEntity: TrendingShow, show: TiviShow, page: Int): TrendingEntry {
        assert(show.id != null)
        return TrendingEntry(showId = show.id!!, page = page, watchers = networkEntity.watchers).apply {
            this.show = show
        }
    }

}