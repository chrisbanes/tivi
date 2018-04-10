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
import com.uwetrottmann.trakt5.TraktV2
import com.uwetrottmann.trakt5.entities.UserSlug
import com.uwetrottmann.trakt5.enums.Extended
import io.reactivex.Completable
import io.reactivex.Flowable
import me.banes.chris.tivi.ShowFetcher
import me.banes.chris.tivi.calls.ListCall
import me.banes.chris.tivi.data.DatabaseTxRunner
import me.banes.chris.tivi.data.daos.WatchedDao
import me.banes.chris.tivi.data.entities.WatchedEntry
import me.banes.chris.tivi.data.entities.WatchedListItem
import me.banes.chris.tivi.extensions.toRxSingle
import me.banes.chris.tivi.util.AppRxSchedulers
import javax.inject.Inject

class WatchedCall @Inject constructor(
    private val databaseTxRunner: DatabaseTxRunner,
    private val watchDao: WatchedDao,
    private val showFetcher: ShowFetcher,
    private val trakt: TraktV2,
    private val schedulers: AppRxSchedulers
) : ListCall<Unit, WatchedListItem> {

    override val pageSize = 21

    fun data() = data(Unit)

    override fun data(param: Unit): Flowable<List<WatchedListItem>> {
        return watchDao.entries()
                .distinctUntilChanged()
                .subscribeOn(schedulers.database)
    }

    override fun dataSourceFactory(): DataSource.Factory<Int, WatchedListItem> = watchDao.entriesDataSource()

    override fun refresh(param: Unit): Completable {
        return trakt.users().watchedShows(UserSlug.ME, Extended.NOSEASONS).toRxSingle()
                .subscribeOn(schedulers.network)
                .toFlowable()
                .flatMapIterable { it }
                .flatMapSingle { traktEntry ->
                    showFetcher.load(traktEntry.show.ids.trakt, traktEntry.show)
                            .map {
                                WatchedEntry(null, it.id!!, traktEntry.last_watched_at)
                            }
                }
                .toList()
                .observeOn(schedulers.database)
                .doOnSuccess {
                    databaseTxRunner.runInTransaction {
                        watchDao.deleteAll()
                        it.forEach { watchDao.insert(it) }
                    }
                }
                .toCompletable()
    }
}