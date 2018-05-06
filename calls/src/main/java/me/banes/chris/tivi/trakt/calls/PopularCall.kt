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

import com.uwetrottmann.trakt5.TraktV2
import com.uwetrottmann.trakt5.entities.Show
import com.uwetrottmann.trakt5.enums.Extended
import me.banes.chris.tivi.ShowFetcher
import me.banes.chris.tivi.api.ItemWithIndex
import me.banes.chris.tivi.calls.PaginatedEntryCallImpl
import me.banes.chris.tivi.data.DatabaseTransactionRunner
import me.banes.chris.tivi.data.daos.PopularDao
import me.banes.chris.tivi.data.daos.TiviShowDao
import me.banes.chris.tivi.data.entities.PopularEntry
import me.banes.chris.tivi.data.entities.PopularListItem
import me.banes.chris.tivi.extensions.fetchBodyWithRetry
import me.banes.chris.tivi.util.AppCoroutineDispatchers
import me.banes.chris.tivi.util.AppRxSchedulers
import javax.inject.Inject

class PopularCall @Inject constructor(
    databaseTransactionRunner: DatabaseTransactionRunner,
    showDao: TiviShowDao,
    popularDao: PopularDao,
    private val showFetcher: ShowFetcher,
    private val trakt: TraktV2,
    schedulers: AppRxSchedulers,
    dispatchers: AppCoroutineDispatchers
) : PaginatedEntryCallImpl<ItemWithIndex<Show>, PopularEntry, PopularListItem, PopularDao>(
        databaseTransactionRunner,
        showDao,
        popularDao,
        showFetcher,
        schedulers,
        dispatchers
) {

    override suspend fun networkCall(page: Int): List<ItemWithIndex<Show>> {
        // We add one to the page since Trakt uses a 1-based index whereas we use 0-based
        return trakt.shows().popular(page + 1, pageSize, Extended.NOSEASONS)
                .fetchBodyWithRetry()
                .mapIndexed { index, show -> ItemWithIndex(show, index) }
    }

    override fun mapToEntry(networkEntity: ItemWithIndex<Show>, showId: Long, page: Int): PopularEntry {
        return PopularEntry(showId = showId, page = page, pageOrder = networkEntity.index)
    }

    override suspend fun insertShowPlaceholder(response: ItemWithIndex<Show>): Long {
        return showFetcher.insertPlaceholderIfNeeded(response.item)
    }
}
