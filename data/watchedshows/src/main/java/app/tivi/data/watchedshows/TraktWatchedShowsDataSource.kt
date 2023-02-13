/*
 * Copyright 2023 Google LLC
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

package app.tivi.data.watchedshows

import app.tivi.data.mappers.TraktBaseShowToTiviShow
import app.tivi.data.mappers.pairMapperOf
import app.tivi.data.models.TiviShow
import app.tivi.data.models.WatchedShowEntry
import app.tivi.data.util.bodyOrThrow
import app.tivi.data.util.toKotlinInstant
import app.tivi.data.util.withRetry
import com.uwetrottmann.trakt5.enums.Extended
import com.uwetrottmann.trakt5.services.Sync
import me.tatarka.inject.annotations.Inject
import retrofit2.awaitResponse

@Inject
class TraktWatchedShowsDataSource(
    private val syncService: Lazy<Sync>,
    showMapper: TraktBaseShowToTiviShow,
) : WatchedShowsDataSource {
    private val responseMapper = pairMapperOf(showMapper) { from ->
        WatchedShowEntry(
            showId = 0,
            lastWatched = from.last_watched_at!!.toKotlinInstant(),
        )
    }

    override suspend operator fun invoke(): List<Pair<TiviShow, WatchedShowEntry>> = withRetry {
        syncService.value
            .watchedShows(Extended.NOSEASONS)
            .awaitResponse()
            .let { responseMapper.invoke(it.bodyOrThrow()) }
    }
}
