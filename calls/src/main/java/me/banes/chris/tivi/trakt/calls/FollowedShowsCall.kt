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
import io.reactivex.Flowable
import me.banes.chris.tivi.calls.ListCall
import me.banes.chris.tivi.data.daos.FollowedShowsDao
import me.banes.chris.tivi.data.entities.FollowedShowsListItem
import me.banes.chris.tivi.util.AppRxSchedulers
import javax.inject.Inject

class FollowedShowsCall @Inject constructor(
    private val followedShowsDao: FollowedShowsDao,
    private val schedulers: AppRxSchedulers
) : ListCall<Unit, FollowedShowsListItem> {

    override val pageSize = 21

    fun data() = data(Unit)

    override fun data(param: Unit): Flowable<List<FollowedShowsListItem>> {
        return followedShowsDao.entries()
                .distinctUntilChanged()
                .subscribeOn(schedulers.database)
    }

    override fun dataSourceFactory(): DataSource.Factory<Int, FollowedShowsListItem> = followedShowsDao.entriesDataSource()

    override suspend fun refresh(param: Unit) = Unit
}