// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.daos

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.tivi.data.Database
import app.tivi.data.models.TiviShow
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
class SqlDelightTiviShowDao(
    override val db: Database,
    private val dispatchers: AppCoroutineDispatchers,
) : TiviShowDao, SqlDelightEntityDao<TiviShow> {

    override fun getShowWithTraktId(id: Int): TiviShow? {
        return db.showQueries.getShowWithTraktId(id, ::TiviShow)
            .executeAsOneOrNull()
    }

    override fun getShowsWithIds(ids: List<Long>): Flow<List<TiviShow>> {
        return db.showQueries.getShowsWithIds(ids, ::TiviShow)
            .asFlow()
            .mapToList(dispatchers.io)
    }

    override fun getShowWithTmdbId(id: Int): TiviShow? {
        return db.showQueries.getShowWithTmdbId(id, ::TiviShow)
            .executeAsOneOrNull()
    }

    override fun getShowWithIdFlow(id: Long): Flow<TiviShow> {
        return db.showQueries.getShowWithId(id, ::TiviShow)
            .asFlow()
            .mapToOne(dispatchers.io)
    }

    override fun getShowWithId(id: Long): TiviShow? {
        return db.showQueries.getShowWithId(id, ::TiviShow)
            .executeAsOneOrNull()
    }

    override fun getTraktIdForShowId(id: Long): Int? {
        return db.showQueries.getTraktIdForShowId(id)
            .executeAsOneOrNull()?.trakt_id
    }

    override fun getTmdbIdForShowId(id: Long): Int? {
        return db.showQueries.getTmdbIdForShowId(id)
            .executeAsOneOrNull()?.tmdb_id
    }

    override fun getImdbIdForShowId(id: Long): String? {
        return db.showQueries.getImdbIdForShowId(id)
            .executeAsOneOrNull()?.imdb_id
    }

    override fun getIdForTraktId(traktId: Int): Long? {
        return db.showQueries.getIdForTraktId(traktId)
            .executeAsOneOrNull()
    }

    override fun getIdForTmdbId(tmdbId: Int): Long? {
        return db.showQueries.getIdForTmdbId(tmdbId)
            .executeAsOneOrNull()
    }

    override fun delete(id: Long) {
        db.showQueries.delete(id)
    }

    override fun deleteAll() {
        db.showQueries.deleteAll()
    }

    override fun deleteEntity(entity: TiviShow) {
        db.showQueries.delete(entity.id)
    }

    override fun insert(entity: TiviShow): Long {
        db.showQueries.insert(
            id = entity.id,
            title = entity.title,
            original_title = entity.originalTitle,
            trakt_id = entity.traktId,
            tmdb_id = entity.tmdbId,
            imdb_id = entity.imdbId,
            overview = entity.summary,
            homepage = entity.homepage,
            trakt_rating = entity.traktRating,
            trakt_votes = entity.traktVotes,
            certification = entity.certification,
            first_aired = entity.firstAired,
            country = entity.country,
            network = entity.network,
            network_logo_path = entity.networkLogoPath,
            runtime = entity.runtime,
            genres = entity._genres,
            status = entity.status,
            airs_day = entity.airsDay,
            airs_time = entity.airsTime,
            airs_tz = entity.airsTimeZone,
        )
        return db.showQueries.lastInsertRowId().executeAsOne()
    }

    override fun update(entity: TiviShow) {
        db.showQueries.update(
            id = entity.id,
            title = entity.title,
            original_title = entity.originalTitle,
            trakt_id = entity.traktId,
            tmdb_id = entity.tmdbId,
            imdb_id = entity.imdbId,
            overview = entity.summary,
            homepage = entity.homepage,
            trakt_rating = entity.traktRating,
            trakt_votes = entity.traktVotes,
            certification = entity.certification,
            first_aired = entity.firstAired,
            country = entity.country,
            network = entity.network,
            network_logo_path = entity.networkLogoPath,
            runtime = entity.runtime,
            genres = entity._genres,
            status = entity.status,
            airs_day = entity.airsDay,
            airs_time = entity.airsTime,
            airs_tz = entity.airsTimeZone,
        )
    }

    override fun search(query: String): List<TiviShow> {
        return db.showQueries.search(query, ::TiviShow).executeAsList()
    }
}
