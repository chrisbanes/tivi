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

package app.tivi.data.mappers

import app.moviebase.trakt.model.TraktMediaItem
import app.tivi.data.models.TiviShow
import app.tivi.data.models.WatchedShowEntry
import me.tatarka.inject.annotations.Inject

@Inject
class TraktBaseShowToWatchedShowEntry(
    private val showMapper: TraktShowToTiviShow,
) : Mapper<TraktMediaItem, Pair<TiviShow, WatchedShowEntry>> {

    override fun map(from: TraktMediaItem): Pair<TiviShow, WatchedShowEntry> {
        val watchedShowEntry = WatchedShowEntry(
            showId = 0,
            lastWatched = from.lastWatchedAt!!,
            lastUpdated = from.lastUpdatedAt!!,
        )
        return showMapper.map(from.show!!) to watchedShowEntry
    }
}
