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

import app.cash.paging.PagingSource
import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.paging3.QueryPagingSource
import app.tivi.data.Database
import app.tivi.data.compoundmodels.RecommendedEntryWithShow
import app.tivi.data.models.RecommendedShowEntry
import app.tivi.data.models.TiviShow
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
class SqlDelightRecommendedShowsDao(
    override val db: Database,
    private val dispatchers: AppCoroutineDispatchers,
) : RecommendedDao, SqlDelightEntityDao<RecommendedShowEntry> {
    override fun entriesForPage(page: Int): Flow<List<RecommendedShowEntry>> {
        return db.recommended_entriesQueries.entriesInPage(page, ::RecommendedShowEntry)
            .asFlow()
            .mapToList(dispatchers.io)
    }

    override fun entriesObservable(count: Int, offset: Int): Flow<List<RecommendedEntryWithShow>> {
        return entriesWithShow(count.toLong(), offset.toLong()).asFlow().mapToList(dispatchers.io)
    }

    override fun entriesPagingSource(): PagingSource<Int, RecommendedEntryWithShow> {
        return QueryPagingSource(
            countQuery = db.recommended_entriesQueries.count(),
            transacter = db.recommended_entriesQueries,
            context = dispatchers.io,
            queryProvider = ::entriesWithShow,
        )
    }

    override fun deletePage(page: Int) {
        db.recommended_entriesQueries.deletePage(page)
    }

    override fun deleteAll() {
        db.recommended_entriesQueries.deleteAll()
    }

    override fun getLastPage(): Int? {
        return db.recommended_entriesQueries.getLastPage().executeAsOne().MAX?.toInt()
    }

    override fun deleteEntity(entity: RecommendedShowEntry) {
        db.recommended_entriesQueries.delete(entity.id)
    }

    override fun insert(entity: RecommendedShowEntry): Long {
        db.recommended_entriesQueries.insert(
            id = entity.id,
            show_id = entity.showId,
            page = entity.page,
        )
        return db.recommended_entriesQueries.lastInsertRowId().executeAsOne()
    }

    override fun update(entity: RecommendedShowEntry) {
        db.recommended_entriesQueries.update(
            id = entity.id,
            show_id = entity.showId,
            page = entity.page,
        )
    }

    private fun entriesWithShow(limit: Long, offset: Long): Query<RecommendedEntryWithShow> {
        return db.recommended_entriesQueries.entriesWithShow(
            limit = limit,
            offset = offset,
            mapper = {
                    id, show_id, page, id_, title, original_title,
                    trakt_id, tmdb_id, imdb_id, overview, homepage, trakt_rating, trakt_votes,
                    certification, first_aired, country, network, network_logo_path, runtime,
                    genres, status, airs_day, airs_time, airs_tz,
                ->
                RecommendedEntryWithShow(
                    entry = RecommendedShowEntry(
                        id = id,
                        showId = show_id,
                        page = page,
                    ),
                    show = TiviShow(
                        id_, title, original_title, trakt_id, tmdb_id, imdb_id, overview, homepage,
                        trakt_rating, trakt_votes, certification, first_aired, country, network,
                        network_logo_path, runtime, genres, status, airs_day, airs_time, airs_tz,
                    ),
                )
            },
        )
    }
}
