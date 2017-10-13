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

import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import io.reactivex.Flowable
import me.banes.chris.tivi.data.Entry
import me.banes.chris.tivi.data.entities.ListItem

abstract class EntryDao<EC : Entry, LI : ListItem<EC>>(protected val showDao: TiviShowDao) {
    abstract fun entries(): Flowable<List<LI>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(entry: EC): Long

    abstract fun deleteAll()
}