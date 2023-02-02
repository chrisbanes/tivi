/*
 * Copyright 2017 Google LLC
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

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import app.tivi.data.entities.TiviShow
import app.tivi.data.repositories.shows.mergeShows
import app.tivi.data.resultentities.ShowDetailed
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TiviShowDao : EntityDao<TiviShow>() {
    @Query("SELECT * FROM shows WHERE trakt_id = :id")
    abstract suspend fun getShowWithTraktId(id: Int): TiviShow?

    @Query("SELECT * FROM shows WHERE id IN (:ids)")
    abstract fun getShowsWithIds(ids: List<Long>): Flow<List<TiviShow>>

    @Query("SELECT * FROM shows WHERE tmdb_id = :id")
    abstract suspend fun getShowWithTmdbId(id: Int): TiviShow?

    @Query("SELECT * FROM shows WHERE id = :id")
    abstract fun getShowWithIdFlow(id: Long): Flow<TiviShow>

    @Transaction
    @Query("SELECT * FROM shows WHERE id = :id")
    abstract suspend fun getShowWithIdDetailed(id: Long): ShowDetailed?

    @Transaction
    @Query("SELECT * FROM shows WHERE id = :id")
    abstract fun getShowDetailedWithIdFlow(id: Long): Flow<ShowDetailed>

    @Query("SELECT * FROM shows WHERE id = :id")
    abstract suspend fun getShowWithId(id: Long): TiviShow?

    suspend fun getShowWithIdOrThrow(id: Long): TiviShow {
        return getShowWithId(id)
            ?: throw IllegalArgumentException("No show with id $id in database")
    }

    @Query("SELECT trakt_id FROM shows WHERE id = :id")
    abstract suspend fun getTraktIdForShowId(id: Long): Int?

    @Query("SELECT tmdb_id FROM shows WHERE id = :id")
    abstract suspend fun getTmdbIdForShowId(id: Long): Int?

    @Query("SELECT id FROM shows WHERE trakt_id = :traktId")
    abstract suspend fun getIdForTraktId(traktId: Int): Long?

    @Query("SELECT id FROM shows WHERE tmdb_id = :tmdbId")
    abstract suspend fun getIdForTmdbId(tmdbId: Int): Long?

    @Query("DELETE FROM shows WHERE id = :id")
    abstract suspend fun delete(id: Long)

    @Query("DELETE FROM shows")
    abstract suspend fun deleteAll()

    suspend fun getIdOrSavePlaceholder(show: TiviShow): Long {
        val idForTraktId: Long? = if (show.traktId != null) getIdForTraktId(show.traktId) else null
        val idForTmdbId: Long? = if (show.tmdbId != null) getIdForTmdbId(show.tmdbId) else null

        if (idForTraktId != null && idForTmdbId != null) {
            return if (idForTmdbId == idForTraktId) {
                // Great, the entities are matching
                idForTraktId
            } else {
                val showForTmdbId = getShowWithIdOrThrow(idForTmdbId)
                val showForTraktId = getShowWithIdOrThrow(idForTraktId)
                deleteEntity(showForTmdbId)
                return insertOrUpdate(mergeShows(showForTraktId, showForTraktId, showForTmdbId))
            }
        }

        if (idForTraktId != null) {
            // If we get here, we only have a entity with the trakt id
            return idForTraktId
        }
        if (idForTmdbId != null) {
            // If we get here, we only have a entity with the tmdb id
            return idForTmdbId
        }

        // TODO add fuzzy search on name or slug

        return insert(show)
    }
}
