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
interface PopularDao {

    @Query("SELECT * FROM shows " +
            "INNER JOIN popular_shows ON popular_shows.show_id = shows.id " +
            "ORDER BY page, page_order")
    fun entries(): Flowable<List<TiviShow>>

    @Query("SELECT * FROM shows " +
            "INNER JOIN popular_shows ON popular_shows.show_id = shows.id " +
            "WHERE page = :page " +
            "ORDER BY page_order")
    fun entriesPage(page: Int): Flowable<List<TiviShow>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(show: PopularEntry): Long

    @Query("DELETE FROM popular_shows WHERE page = :page")
    fun deletePage(page: Int)

    @Query("DELETE FROM popular_shows")
    fun deleteAll()

    @Query("SELECT MAX(page) from popular_shows")
    fun getLastPage(): Single<Int>

}