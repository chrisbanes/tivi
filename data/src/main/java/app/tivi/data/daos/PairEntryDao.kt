/*
 * Copyright 2018 Google LLC
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

package app.tivi.data.daos

import app.tivi.data.MultipleEntry
import app.tivi.data.resultentities.EntryWithShow
import kotlinx.coroutines.flow.Flow

/**
 * This interface represents a DAO which contains entities which are part of a collective list for a given show.
 */
abstract class PairEntryDao<EC : MultipleEntry, LI : EntryWithShow<EC>> : EntityDao<EC>() {
    abstract fun entries(showId: Long): List<EC>
    abstract fun entriesWithShows(showId: Long): List<LI>
    abstract fun entriesWithShowsObservable(showId: Long): Flow<List<LI>>
    abstract suspend fun deleteWithShowId(showId: Long)
}
