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

package app.tivi.data.repositories.watchedshows

import app.tivi.data.entities.WatchedShowEntry
import app.tivi.data.mappers.TraktShowToTiviShow
import app.tivi.data.resultentities.WatchedShowEntryWithShow
import app.tivi.extensions.fetchBodyWithRetry
import com.uwetrottmann.trakt5.entities.UserSlug
import com.uwetrottmann.trakt5.enums.Extended
import com.uwetrottmann.trakt5.services.Users
import javax.inject.Inject
import javax.inject.Provider

class TraktWatchedShowsDataSource @Inject constructor(
    private val usersService: Provider<Users>,
    private val mapper: TraktShowToTiviShow
) : WatchedShowsDataSource {
    override suspend fun getWatchedShows(): List<WatchedShowEntryWithShow> {
        val results = usersService.get().watchedShows(UserSlug.ME, Extended.NOSEASONS).fetchBodyWithRetry()

        return results.map { watchedShow ->
            WatchedShowEntryWithShow().apply {
                relations = listOf(mapper.map(watchedShow.show))
                entry = WatchedShowEntry(showId = 0, lastWatched = watchedShow.last_watched_at)
            }
        }
    }
}