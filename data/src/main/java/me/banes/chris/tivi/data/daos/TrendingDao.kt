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

package me.banes.chris.tivi.data.daos

import android.arch.paging.DataSource
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import android.arch.persistence.room.Transaction
import io.reactivex.Flowable
import io.reactivex.Single
import me.banes.chris.tivi.data.entities.TrendingEntry
import me.banes.chris.tivi.data.entities.TrendingListItem

@Dao
abstract class TrendingDao : PaginatedEntryDao<TrendingEntry, TrendingListItem> {
    @Transaction
    @Query("SELECT * FROM trending_shows ORDER BY page ASC, watchers DESC, id ASC")
    abstract override fun entries(): Flowable<List<TrendingListItem>>

    @Transaction
    @Query("SELECT * FROM trending_shows ORDER BY page ASC, watchers DESC, id ASC")
    abstract override fun entriesDataSource(): DataSource.Factory<Int, TrendingListItem>

    @Transaction
    @Query("SELECT * FROM trending_shows WHERE page = :page ORDER BY watchers DESC, id ASC")
    abstract override fun entriesPage(page: Int): Flowable<List<TrendingListItem>>

    @Query("DELETE FROM trending_shows WHERE page = :page")
    abstract override fun deletePage(page: Int)

    @Query("DELETE FROM trending_shows")
    abstract override fun deleteAll()

    @Query("SELECT MAX(page) from trending_shows")
    abstract override fun getLastPage(): Single<Int>
}