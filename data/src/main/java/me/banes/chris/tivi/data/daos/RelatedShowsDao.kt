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

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import android.arch.persistence.room.Transaction
import io.reactivex.Flowable
import me.banes.chris.tivi.data.entities.RelatedShowEntry
import me.banes.chris.tivi.data.entities.RelatedShowsListItem

@Dao
abstract class RelatedShowsDao : PairEntryDao<RelatedShowEntry, RelatedShowsListItem> {
    @Transaction
    @Query("SELECT * FROM related_shows WHERE show_id = :showId ORDER BY order_index")
    abstract override fun entries(showId: Long): Flowable<List<RelatedShowsListItem>>

    @Query("DELETE FROM related_shows WHERE show_id = :showId")
    abstract override fun deleteAll(showId: Long)
}