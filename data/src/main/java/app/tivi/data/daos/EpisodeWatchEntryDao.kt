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

package app.tivi.data.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import app.tivi.data.entities.EpisodeWatchEntry
import io.reactivex.Flowable

@Dao
abstract class EpisodeWatchEntryDao : EntityDao<EpisodeWatchEntry> {
    @Query("SELECT * FROM episode_watch_entries WHERE episode_id = :episodeId")
    abstract fun watchesForEpisode(episodeId: Long): List<EpisodeWatchEntry>

    @Query("SELECT * FROM episode_watch_entries WHERE episode_id = :episodeId")
    abstract fun watchesForEpisodeFlowable(episodeId: Long): Flowable<List<EpisodeWatchEntry>>

    @Query("SELECT * FROM episode_watch_entries WHERE trakt_id = :traktId")
    abstract fun entryWithTraktId(traktId: Long): EpisodeWatchEntry?

    @Query("SELECT trakt_id FROM episode_watch_entries")
    abstract fun entryTraktIds(): List<Long>

    @Query("DELETE FROM episode_watch_entries WHERE trakt_id = :traktId")
    abstract fun deleteWithTraktId(traktId: Long)
}