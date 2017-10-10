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

package me.banes.chris.tivi.data

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface TrendingDao : PaginatedEntryDao<TiviShow, TrendingEntry> {

    @Query("SELECT * FROM shows " +
            "INNER JOIN trending_shows ON trending_shows.show_id = shows.id " +
            "ORDER BY page, page_order")
    override fun entries(): Flowable<List<TiviShow>>

    @Query("SELECT * FROM shows " +
            "INNER JOIN trending_shows ON trending_shows.show_id = shows.id " +
            "WHERE page = :page " +
            "ORDER BY page_order")
    override fun entriesPage(page: Int): Flowable<List<TiviShow>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    override fun insert(show: TrendingEntry): Long

    @Query("DELETE FROM trending_shows WHERE page = :page")
    override fun deletePage(page: Int)

    @Query("DELETE FROM trending_shows")
    override fun deleteAll()

    @Query("SELECT MAX(page) from trending_shows")
    override fun getLastPage(): Single<Int>

}