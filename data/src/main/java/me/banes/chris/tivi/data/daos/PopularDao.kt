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

import android.arch.paging.LivePagedListProvider
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import android.arch.persistence.room.Transaction
import io.reactivex.Flowable
import io.reactivex.Single
import me.banes.chris.tivi.data.entities.PopularEntry
import me.banes.chris.tivi.data.entities.PopularListItem

@Dao
abstract class PopularDao : PaginatedEntryDao<PopularEntry, PopularListItem> {
    @Transaction
    @Query("SELECT * FROM popular_shows ORDER BY page, page_order")
    abstract override fun entries(): Flowable<List<PopularListItem>>

    @Transaction
    @Query("SELECT * FROM popular_shows ORDER BY page, page_order")
    abstract override fun entriesLiveList(): LivePagedListProvider<Int, PopularListItem>

    @Transaction
    @Query("SELECT * FROM popular_shows WHERE page = :page")
    abstract override fun entriesPage(page: Int): Flowable<List<PopularListItem>>

    @Query("DELETE FROM popular_shows WHERE page = :page")
    abstract override fun deletePage(page: Int)

    @Query("DELETE FROM popular_shows")
    abstract override fun deleteAll()

    @Query("SELECT MAX(page) from popular_shows")
    abstract override fun getLastPage(): Single<Int>
}