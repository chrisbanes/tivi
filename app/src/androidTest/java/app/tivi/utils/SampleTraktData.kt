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

package app.tivi.utils

import com.uwetrottmann.trakt5.entities.Episode
import com.uwetrottmann.trakt5.entities.EpisodeIds
import com.uwetrottmann.trakt5.entities.HistoryEntry
import com.uwetrottmann.trakt5.entities.SyncErrors
import com.uwetrottmann.trakt5.entities.SyncResponse
import com.uwetrottmann.trakt5.entities.SyncStats
import com.uwetrottmann.trakt5.enums.HistoryType

val traktEpisode1 = Episode().apply {
    ids = EpisodeIds.trakt(episodeOne.traktId!!)
    season = seasonOne.number
    number = episodeOne.number
}

val traktHistoryEntry1 = HistoryEntry().apply {
    id = episodeWatch1.traktId
    watched_at = episodeWatch1.watchedAt
    action = "watch"
    type = HistoryType.EPISODES.toString()
    episode = traktEpisode1
}

val traktHistoryEntry2 = HistoryEntry().apply {
    id = episodeWatch2.traktId
    watched_at = episodeWatch2.watchedAt
    action = "watch"
    type = HistoryType.EPISODES.toString()
    episode = traktEpisode1
}

fun syncResponse(added: Int = 0, existing: Int = 0 , deleted: Int = 0, notFound: Int = 0) = SyncResponse().apply {
    this.added = SyncStats().apply { episodes = added }
    this.existing = SyncStats().apply { episodes = existing }
    this.deleted = SyncStats().apply { episodes = deleted }
    this.not_found = SyncErrors().apply { episodes = emptyList() }
}