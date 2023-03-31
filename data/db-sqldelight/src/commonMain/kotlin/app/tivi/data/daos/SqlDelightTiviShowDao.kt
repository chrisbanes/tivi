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

import app.tivi.data.Database
import app.tivi.data.models.TiviShow
import kotlinx.coroutines.flow.Flow

class SqlDelightTiviShowDao(
    private val db: Database
): TiviShowDao {

    private fun mapToTiviShow(
        id: Long,
        title: String?,
        original_title: String?,
        trakt_id: Long?,
        tmdb_id: Long?,
        imdb_id: String?,
        overview: String?,
        homepage: String?,
        trakt_rating: Double?,
        trakt_votes: Long?,
        certification: String?,
        first_aired: String?,
        country: String?,
        network: String?,
        network_logo_path: String?,
        runtime: Long?,
        genres: String?,
        status: String?,
        airs_day: Long?,
        airs_time: String?,
        airs_tz: String?,
    ): TiviShow = TiviShow(
        id,
        title,
        original_title,
        trakt_id?.toInt(),
        tmdb_id?.toInt(),
        imdb_id,
        overview,
        homepage,
        trakt_rating?.toFloat(),
        trakt_votes?.toInt(),
        certification,
        first_aired,

    )

    override suspend fun getShowWithTraktId(id: Int): TiviShow? {
        return db.showsQueries.getShowWithTraktId(id.toLong(), ::TiviShow).executeAsOneOrNull()
    }

    override fun getShowsWithIds(ids: List<Long>): Flow<List<TiviShow>> {
        TODO("Not yet implemented")
    }

    override suspend fun getShowWithTmdbId(id: Int): TiviShow? {
        TODO("Not yet implemented")
    }

    override fun getShowWithIdFlow(id: Long): Flow<TiviShow> {
        TODO("Not yet implemented")
    }

    override suspend fun getShowWithId(id: Long): TiviShow? {
        TODO("Not yet implemented")
    }

    override suspend fun getTraktIdForShowId(id: Long): Int? {
        TODO("Not yet implemented")
    }

    override suspend fun getTmdbIdForShowId(id: Long): Int? {
        TODO("Not yet implemented")
    }

    override suspend fun getIdForTraktId(traktId: Int): Long? {
        TODO("Not yet implemented")
    }

    override suspend fun getIdForTmdbId(tmdbId: Int): Long? {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAll() {
        TODO("Not yet implemented")
    }

    override suspend fun upsert(entity: TiviShow): Long {
        TODO("Not yet implemented")
    }

    override suspend fun upsertAll(vararg entity: TiviShow) {
        TODO("Not yet implemented")
    }

    override suspend fun upsertAll(entities: List<TiviShow>) {
        TODO("Not yet implemented")
    }

    override suspend fun update(entity: TiviShow) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteEntity(entity: TiviShow): Int {
        TODO("Not yet implemented")
    }
}
