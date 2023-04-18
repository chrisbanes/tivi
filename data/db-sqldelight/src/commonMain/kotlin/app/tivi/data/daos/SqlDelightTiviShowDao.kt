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

package app.tivi.data.daos

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.tivi.data.Database
import app.tivi.data.awaitAsNull
import app.tivi.data.models.TiviShow
import app.tivi.data.upsert
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class SqlDelightTiviShowDao(
    private val db: Database,
    private val dispatchers: AppCoroutineDispatchers,
) : TiviShowDao {

    override suspend fun getShowWithTraktId(id: Int): TiviShow? {
        return db.showQueries.getShowWithTraktId(id, ::TiviShow)
            .awaitAsNull(dispatchers.io)
    }

    override fun getShowsWithIds(ids: List<Long>): Flow<List<TiviShow>> {
        return db.showQueries.getShowsWithIds(ids, ::TiviShow)
            .asFlow()
            .mapToList(dispatchers.io)
    }

    override suspend fun getShowWithTmdbId(id: Int): TiviShow? {
        return db.showQueries.getShowWithTmdbId(id, ::TiviShow)
            .awaitAsNull(dispatchers.io)
    }

    override fun getShowWithIdFlow(id: Long): Flow<TiviShow> {
        return db.showQueries.getShowWithId(id, ::TiviShow)
            .asFlow()
            .mapToOne(dispatchers.io)
    }

    override suspend fun getShowWithId(id: Long): TiviShow? {
        return db.showQueries.getShowWithId(id, ::TiviShow)
            .awaitAsNull(dispatchers.io)
    }

    override suspend fun getTraktIdForShowId(id: Long): Int? {
        return db.showQueries.getTraktIdForShowId(id)
            .awaitAsNull(dispatchers.io)?.trakt_id
    }

    override suspend fun getTmdbIdForShowId(id: Long): Int? {
        return db.showQueries.getTmdbIdForShowId(id)
            .awaitAsNull(dispatchers.io)?.tmdb_id
    }

    override suspend fun getIdForTraktId(traktId: Int): Long? {
        return db.showQueries.getIdForTraktId(traktId)
            .awaitAsNull(dispatchers.io)
    }

    override suspend fun getIdForTmdbId(tmdbId: Int): Long? {
        return db.showQueries.getIdForTmdbId(tmdbId)
            .awaitAsNull(dispatchers.io)
    }

    override suspend fun delete(id: Long) = withContext(dispatchers.io) {
        db.showQueries.delete(id)
    }

    override suspend fun deleteAll() = withContext(dispatchers.io) {
        db.showQueries.deleteAll()
    }

    override suspend fun upsert(entity: TiviShow): Long = withContext(dispatchers.io) {
        upsertShow(entity)
    }

    override suspend fun upsertAll(entities: List<TiviShow>) = withContext(dispatchers.io) {
        db.transaction {
            for (entity in entities) {
                upsertShow(entity)
            }
        }
    }

    override suspend fun deleteEntity(entity: TiviShow) = withContext(dispatchers.io) {
        db.showQueries.delete(entity.id)
    }

    private fun upsertShow(entity: TiviShow): Long = db.showQueries.upsert(
        entity = entity,
        insert = { show ->
            insert(
                id = show.id,
                title = show.title,
                original_title = show.originalTitle,
                trakt_id = show.traktId,
                tmdb_id = show.tmdbId,
                imdb_id = show.imdbId,
                overview = show.summary,
                homepage = show.homepage,
                trakt_rating = show.traktRating,
                trakt_votes = show.traktVotes,
                certification = show.certification,
                first_aired = show.firstAired,
                country = show.country,
                network = show.network,
                network_logo_path = show.networkLogoPath,
                runtime = show.runtime,
                genres = show._genres,
                status = show.status,
                airs_day = show.airsDay,
                airs_time = show.airsTime,
                airs_tz = show.airsTimeZone,
            )
        },
        update = { show ->
            update(
                id = show.id,
                title = show.title,
                original_title = show.originalTitle,
                trakt_id = show.traktId,
                tmdb_id = show.tmdbId,
                imdb_id = show.imdbId,
                overview = show.summary,
                homepage = show.homepage,
                trakt_rating = show.traktRating,
                trakt_votes = show.traktVotes,
                certification = show.certification,
                first_aired = show.firstAired,
                country = show.country,
                network = show.network,
                network_logo_path = show.networkLogoPath,
                runtime = show.runtime,
                genres = show._genres,
                status = show.status,
                airs_day = show.airsDay,
                airs_time = show.airsTime,
                airs_tz = show.airsTimeZone,
            )
        },
        lastInsertRowId = { lastInsertRowId().executeAsOne() },
    )
}
