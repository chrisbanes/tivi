// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.daos

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.tivi.data.Database
import app.tivi.data.compoundmodels.RelatedShowEntryWithShow
import app.tivi.data.models.RelatedShowEntry
import app.tivi.data.models.TiviShow
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
class SqlDelightRelatedShowsDao(
    override val db: Database,
    private val dispatchers: AppCoroutineDispatchers,
) : RelatedShowsDao, SqlDelightEntityDao<RelatedShowEntry> {

    override fun entriesObservable(showId: Long): Flow<List<RelatedShowEntry>> {
        return db.related_showsQueries.entries(showId, ::RelatedShowEntry)
            .asFlow()
            .mapToList(dispatchers.io)
    }

    override fun entriesWithShowsObservable(showId: Long): Flow<List<RelatedShowEntryWithShow>> {
        return db.related_showsQueries.entriesWithShows(showId) {
                id, show_id, other_show_id, order_index,
                id_, title, original_title, trakt_id, tmdb_id, imdb_id, overview, homepage,
                trakt_rating, trakt_votes, certification, first_aired, country, network,
                network_logo_path, runtime, genres, status, airs_day, airs_time, airs_tz,
            ->
            RelatedShowEntryWithShow(
                entry = RelatedShowEntry(id, show_id, other_show_id, order_index),
                show = TiviShow(
                    id_, title, original_title, trakt_id, tmdb_id, imdb_id, overview, homepage,
                    trakt_rating, trakt_votes, certification, first_aired, country, network,
                    network_logo_path, runtime, genres, status, airs_day, airs_time, airs_tz,
                ),
            )
        }.asFlow().mapToList(dispatchers.io)
    }

    override fun deleteWithShowId(showId: Long) {
        db.related_showsQueries.deleteWithShowId(showId)
    }

    override fun insert(entity: RelatedShowEntry): Long {
        db.related_showsQueries.insert(
            id = entity.id,
            show_id = entity.showId,
            other_show_id = entity.otherShowId,
            order_index = entity.orderIndex,
        )
        return db.related_showsQueries.lastInsertRowId().executeAsOne()
    }

    override fun update(entity: RelatedShowEntry) {
        db.related_showsQueries.update(
            id = entity.id,
            show_id = entity.showId,
            other_show_id = entity.otherShowId,
            order_index = entity.orderIndex,
        )
    }

    override fun deleteEntity(entity: RelatedShowEntry) {
        db.related_showsQueries.delete(entity.id)
    }

    override fun deleteAll() {
        db.related_showsQueries.deleteAll()
    }
}
