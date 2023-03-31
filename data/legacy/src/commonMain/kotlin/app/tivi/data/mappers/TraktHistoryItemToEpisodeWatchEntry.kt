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

import app.moviebase.trakt.model.TraktHistoryItem
import app.tivi.data.models.EpisodeWatchEntry
import me.tatarka.inject.annotations.Inject

@Inject
class TraktHistoryItemToEpisodeWatchEntry() : Mapper<TraktHistoryItem, EpisodeWatchEntry> {
    override suspend fun map(from: TraktHistoryItem) = EpisodeWatchEntry(
        episodeId = 0,
        traktId = from.id?.toLong(),
        watchedAt = requireNotNull(from.watchedAt),
    )
}
