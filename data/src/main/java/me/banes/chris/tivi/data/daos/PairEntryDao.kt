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

import io.reactivex.Flowable
import me.banes.chris.tivi.data.MultipleEntry
import me.banes.chris.tivi.data.entities.ListItem

/**
 * This interface represents a DAO which contains entities which are part of a collective list for a given show.
 */
interface PairEntryDao<EC : MultipleEntry, LI : ListItem<EC>> : EntityDao<EC> {
    fun entries(showId: Long): Flowable<List<LI>>
    fun deleteAll(showId: Long)
}