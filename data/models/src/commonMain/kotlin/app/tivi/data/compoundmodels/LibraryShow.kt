/*
 * Copyright 2022 Google LLC
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

package app.tivi.data.compoundmodels

import app.tivi.data.models.TiviShow
import app.tivi.data.models.WatchedShowEntry
import app.tivi.data.views.ShowsWatchStats

@Suppress("PropertyName")
class LibraryShow {

    lateinit var show: TiviShow

    lateinit var _watchedEntities: List<WatchedShowEntry>

    lateinit var _stats: List<ShowsWatchStats>

    val watchedEntry: WatchedShowEntry? get() = _watchedEntities.firstOrNull()

    val stats: ShowsWatchStats? get() = _stats.firstOrNull()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LibraryShow

        if (show != other.show) return false
        if (_watchedEntities != other._watchedEntities) return false
        if (_stats != other._stats) return false

        return true
    }

    override fun hashCode(): Int {
        var result = show.hashCode()
        result = 31 * result + _watchedEntities.hashCode()
        result = 31 * result + _stats.hashCode()
        return result
    }
}
