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

import app.tivi.data.models.TiviShow
import app.tivi.data.util.mergeShows
import kotlinx.coroutines.flow.Flow

interface TiviShowDao : EntityDao<TiviShow> {

    fun getShowWithTraktId(id: Int): TiviShow?

    fun getShowsWithIds(ids: List<Long>): Flow<List<TiviShow>>

    fun getShowWithTmdbId(id: Int): TiviShow?

    fun getShowWithIdFlow(id: Long): Flow<TiviShow>

    fun getShowWithId(id: Long): TiviShow?

    fun getTraktIdForShowId(id: Long): Int?

    fun getTmdbIdForShowId(id: Long): Int?

    fun getIdForTraktId(traktId: Int): Long?

    fun getIdForTmdbId(tmdbId: Int): Long?

    fun delete(id: Long)

    fun deleteAll()

    fun search(query: String): List<TiviShow>
}

fun TiviShowDao.getShowWithIdOrThrow(id: Long): TiviShow {
    return getShowWithId(id)
        ?: throw IllegalArgumentException("No show with id $id in database")
}

fun TiviShowDao.getIdOrSavePlaceholder(show: TiviShow): Long {
    val idForTraktId: Long? = show.traktId?.let { getIdForTraktId(it) }
    val idForTmdbId: Long? = show.tmdbId?.let { getIdForTmdbId(it) }
    return when {
        idForTraktId != null && idForTmdbId != null -> {
            if (idForTmdbId == idForTraktId) {
                // Great, the entities are matching
                idForTraktId
            } else {
                // Otherwise we have 2 different entities. Remove one
                val showForTmdbId = getShowWithIdOrThrow(idForTmdbId)
                val showForTraktId = getShowWithIdOrThrow(idForTraktId)
                deleteEntity(showForTmdbId)
                return upsert(mergeShows(showForTraktId, showForTraktId, showForTmdbId))
            }
        }
        // We only have a entity with the trakt id
        idForTraktId != null -> idForTraktId
        // We only have a entity with the tmdb id
        idForTmdbId != null -> idForTmdbId
        // TODO add fuzzy search on name or slug
        else -> return upsert(show)
    }
}
