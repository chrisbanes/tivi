/*
 * Copyright 2018 Google, Inc.
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
import io.reactivex.Completable
import io.reactivex.Flowable
import me.banes.chris.tivi.calls.ListCall
import me.banes.chris.tivi.data.daos.MyShowsDao
import me.banes.chris.tivi.data.daos.WatchedDao
import me.banes.chris.tivi.data.entities.MyShowsEntry
import me.banes.chris.tivi.data.entities.MyShowsListItem
import me.banes.chris.tivi.data.entities.WatchedListItem
import me.banes.chris.tivi.util.AppRxSchedulers
import javax.inject.Inject

class MyShowsCall @Inject constructor(
    private val myShowsDao: MyShowsDao,
    private val watchedDao: WatchedDao,
    private val schedulers: AppRxSchedulers
) : ListCall<Unit, MyShowsListItem> {

    override val pageSize = 21

    fun data() = data(Unit)

    override fun data(param: Unit): Flowable<List<MyShowsListItem>> {
        return myShowsDao.entries()
                .distinctUntilChanged()
                .subscribeOn(schedulers.database)
    }

    override fun dataSourceFactory(): DataSource.Factory<Int, MyShowsListItem> = myShowsDao.entriesDataSource()

    override fun refresh(param: Unit): Completable {
        // TODO, remove this. Just for testing
        return watchedDao.entries()
                .firstElement()
                .map {
                    ArrayList(it).apply { shuffle() }
                            .take(10)
                            .mapNotNull(WatchedListItem::show)
                }
                .doOnSuccess {
                    myShowsDao.deleteAll()
                    it.forEach {
                        myShowsDao.insert(MyShowsEntry(showId = it.id!!))
                    }
                }
                .ignoreElement()
    }
}