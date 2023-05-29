// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

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

    fun getImdbIdForShowId(id: Long): String?

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
