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
import app.tivi.data.Database
import app.tivi.data.compoundmodels.RelatedShowEntryWithShow
import app.tivi.data.models.RelatedShowEntry
import app.tivi.data.models.ShowStatus
import app.tivi.data.models.TiviShow
import app.tivi.data.upsert
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import me.tatarka.inject.annotations.Inject

@Inject
class SqlDelightRelatedShowsDao(
    override val db: Database,
    override val dispatchers: AppCoroutineDispatchers,
) : RelatedShowsDao, SqlDelightEntityDao<RelatedShowEntry> {

    override fun entriesObservable(showId: Long): Flow<List<RelatedShowEntry>> {
        return db.related_showsQueries.entries(showId, ::RelatedShowEntry)
            .asFlow()
            .mapToList(dispatchers.io)
    }

    override fun entriesWithShowsObservable(showId: Long): Flow<List<RelatedShowEntryWithShow>> {
        return db.related_showsQueries.entriesWithShows(showId) {
                id: Long, show_id: Long, other_show_id: Long, order_index: Int,
                id_: Long, title: String?, original_title: String?, trakt_id: Int?, tmdb_id: Int?,
                imdb_id: String?, overview: String?, homepage: String?, trakt_rating: Float?,
                trakt_votes: Int?, certification: String?, first_aired: Instant?, country: String?,
                network: String?, network_logo_path: String?, runtime: Int?, genres: String?,
                status: ShowStatus?, airs_day: DayOfWeek?, airs_time: LocalTime?,
                airs_tz: TimeZone?,
            ->

            val entry = RelatedShowEntry(id, show_id, other_show_id, order_index)
            val show = TiviShow(
                id_, title, original_title, trakt_id, tmdb_id, imdb_id, overview, homepage,
                trakt_rating, trakt_votes, certification, first_aired, country, network,
                network_logo_path, runtime, genres, status, airs_day, airs_time, airs_tz,
            )

            RelatedShowEntryWithShow().apply {
                this.entry = entry
                relations = listOf(show)
            }
        }.asFlow().mapToList(dispatchers.io)
    }

    override suspend fun deleteWithShowId(showId: Long) = withContext(dispatchers.io) {
        db.related_showsQueries.deleteWithShowId(showId)
    }

    override fun upsertBlocking(entity: RelatedShowEntry): Long = db.related_showsQueries.upsert(
        entity = entity,
        insert = { entry ->
            insert(
                id = entry.id,
                show_id = entry.showId,
                other_show_id = entry.otherShowId,
                order_index = entry.orderIndex,
            )
        },
        update = { entry ->
            update(
                id = entry.id,
                show_id = entry.showId,
                other_show_id = entry.otherShowId,
                order_index = entry.orderIndex,
            )
        },
        lastInsertRowId = { lastInsertRowId().executeAsOne() },
    )

    override suspend fun deleteEntity(entity: RelatedShowEntry) = withContext(dispatchers.io) {
        db.related_showsQueries.delete(entity.id)
    }

    override suspend fun deleteAll() = withContext(dispatchers.io) {
        db.related_showsQueries.deleteAll()
    }
}
