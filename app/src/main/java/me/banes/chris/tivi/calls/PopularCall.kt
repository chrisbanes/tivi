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
import com.uwetrottmann.trakt5.entities.Show
import com.uwetrottmann.trakt5.enums.Extended
import io.reactivex.Maybe
import io.reactivex.Single
import me.banes.chris.tivi.api.ItemWithIndex
import me.banes.chris.tivi.data.daos.PopularDao
import me.banes.chris.tivi.data.daos.TiviShowDao
import me.banes.chris.tivi.data.entities.PopularEntry
import me.banes.chris.tivi.data.entities.TiviShow
import me.banes.chris.tivi.extensions.toRxSingle
import me.banes.chris.tivi.util.AppRxSchedulers
import me.banes.chris.tivi.util.DatabaseTxRunner
import javax.inject.Inject

class PopularCall @Inject constructor(
        databaseTxRunner: DatabaseTxRunner,
        showDao: TiviShowDao,
        popularDao: PopularDao,
        tmdbShowFetcher: TmdbShowFetcher,
        trakt: TraktV2,
        schedulers: AppRxSchedulers
) : PaginatedEntryCallImpl<ItemWithIndex<Show>, PopularEntry, PopularDao>(databaseTxRunner, showDao, popularDao, trakt, schedulers, tmdbShowFetcher) {

    override fun networkCall(page: Int): Single<List<ItemWithIndex<Show>>> {
        // We add one to the page since Trakt uses a 1-based index whereas we use 0-based
        return trakt.shows().popular(page + 1, pageSize, Extended.NOSEASONS)
                .toRxSingle()
                .map { it.mapIndexed { index, show -> ItemWithIndex(show, index) } }
    }

    override fun filterResponse(response: ItemWithIndex<Show>): Boolean {
        return response.item.ids.tmdb != null
    }

    override fun mapToEntry(networkEntity: ItemWithIndex<Show>, show: TiviShow, page: Int): PopularEntry {
        assert(show.id != null)
        return PopularEntry(showId = show.id!!, page = page, pageOrder = networkEntity.index).apply {
            this.show = show
        }
    }

    override fun loadShow(response: ItemWithIndex<Show>): Maybe<TiviShow> {
        return response.item.let {
            tmdbShowFetcher.showFromTmdb(it.ids.tmdb, it.ids.trakt)
        }
    }

}
