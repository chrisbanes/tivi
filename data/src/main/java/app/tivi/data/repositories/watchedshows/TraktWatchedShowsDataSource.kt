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

package app.tivi.data.repositories.watchedshows

import app.tivi.data.entities.Result
import app.tivi.data.entities.TiviShow
import app.tivi.data.entities.WatchedShowEntry
import app.tivi.data.mappers.Mapper
import app.tivi.data.mappers.TraktBaseShowToTiviShow
import app.tivi.data.mappers.pairMapperOf
import app.tivi.extensions.executeWithRetry
import app.tivi.extensions.toResult
import com.uwetrottmann.trakt5.entities.BaseShow
import com.uwetrottmann.trakt5.enums.Extended
import com.uwetrottmann.trakt5.services.Sync
import javax.inject.Inject
import javax.inject.Provider

class TraktWatchedShowsDataSource @Inject constructor(
    private val syncService: Provider<Sync>,
    showMapper: TraktBaseShowToTiviShow
) {
    private val entryMapper = object : Mapper<BaseShow, WatchedShowEntry> {
        override suspend fun map(from: BaseShow): WatchedShowEntry {
            return WatchedShowEntry(showId = 0, lastWatched = from.last_watched_at!!)
        }
    }
    private val responseMapper = pairMapperOf(showMapper, entryMapper)

    suspend operator fun invoke(): Result<List<Pair<TiviShow, WatchedShowEntry>>> {
        return syncService.get().watchedShows(Extended.NOSEASONS)
            .executeWithRetry()
            .toResult(responseMapper)
    }
}
