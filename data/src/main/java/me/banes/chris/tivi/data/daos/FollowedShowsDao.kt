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

package me.banes.chris.tivi.data.daos

import android.arch.paging.DataSource
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import android.arch.persistence.room.Transaction
import io.reactivex.Flowable
import me.banes.chris.tivi.data.entities.FollowedShowEntry
import me.banes.chris.tivi.data.entities.FollowedShowsListItem

@Dao
abstract class FollowedShowsDao : EntryDao<FollowedShowEntry, FollowedShowsListItem> {
    @Transaction
    @Query("SELECT * FROM myshows_entries")
    abstract fun entriesBlocking(): List<FollowedShowEntry>

    @Transaction
    @Query("SELECT * FROM myshows_entries")
    abstract override fun entries(): Flowable<List<FollowedShowsListItem>>

    @Transaction
    @Query("SELECT * FROM myshows_entries")
    abstract override fun entriesDataSource(): DataSource.Factory<Int, FollowedShowsListItem>

    @Query("DELETE FROM myshows_entries")
    abstract override fun deleteAll()

    @Query("SELECT * FROM myshows_entries WHERE id = :id")
    abstract fun entryWithId(id: Long): FollowedShowsListItem?

    @Query("SELECT * FROM myshows_entries WHERE show_id = :showId")
    abstract fun entryWithShowId(showId: Long): FollowedShowsListItem?

    @Query("DELETE FROM myshows_entries WHERE show_id = :showId")
    abstract fun deleteWithShowId(showId: Long)

    @Query("SELECT COUNT(*) FROM myshows_entries WHERE show_id = :showId")
    abstract fun entryCountWithShowId(showId: Long): Flowable<Int>
}