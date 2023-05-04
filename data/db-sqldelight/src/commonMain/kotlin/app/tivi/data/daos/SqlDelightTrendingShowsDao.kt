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
import app.tivi.data.compoundmodels.TrendingEntryWithShow
import app.tivi.data.models.TiviShow
import app.tivi.data.models.TrendingShowEntry
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
class SqlDelightTrendingShowsDao(
    override val db: Database,
    private val dispatchers: AppCoroutineDispatchers,
) : TrendingDao, SqlDelightEntityDao<TrendingShowEntry> {
    override fun entriesObservable(page: Int): Flow<List<TrendingShowEntry>> {
        return db.trending_showsQueries.entriesInPage(page, ::TrendingShowEntry)
            .asFlow()
            .mapToList(dispatchers.io)
    }

    override fun entriesObservable(count: Int, offset: Int): Flow<List<TrendingEntryWithShow>> {
        return entriesWithShow(count.toLong(), offset.toLong()).asFlow().mapToList(dispatchers.io)
    }

    override fun entriesPagingSource(): PagingSource<Int, TrendingEntryWithShow> {
        return QueryPagingSource(
            countQuery = db.trending_showsQueries.count(),
            transacter = db.trending_showsQueries,
            context = dispatchers.io,
            queryProvider = ::entriesWithShow,
        )
    }

    override fun deletePage(page: Int) {
        db.trending_showsQueries.deletePage(page)
    }

    override fun deleteAll() {
        db.trending_showsQueries.deleteAll()
    }

    override fun getLastPage(): Int? {
        return db.trending_showsQueries.getLastPage().executeAsOne().MAX?.toInt()
    }

    override fun deleteEntity(entity: TrendingShowEntry) {
        db.trending_showsQueries.delete(entity.id)
    }

    override fun insert(entity: TrendingShowEntry): Long {
        db.trending_showsQueries.insert(
            id = entity.id,
            show_id = entity.showId,
            page = entity.page,
            watchers = entity.watchers,
        )
        return db.trending_showsQueries.lastInsertRowId().executeAsOne()
    }

    override fun update(entity: TrendingShowEntry) {
        db.trending_showsQueries.update(
            id = entity.id,
            show_id = entity.showId,
            page = entity.page,
            watchers = entity.watchers,
        )
    }

    private fun entriesWithShow(limit: Long, offset: Long): Query<TrendingEntryWithShow> {
        return db.trending_showsQueries.entriesWithShow(
            limit = limit,
            offset = offset,
            mapper = {
                    id, show_id, page, watchers, id_, title, original_title,
                    trakt_id, tmdb_id, imdb_id, overview, homepage, trakt_rating, trakt_votes,
                    certification, first_aired, country, network, network_logo_path, runtime, genres,
                    status, airs_day, airs_time, airs_tz,
                ->
                TrendingEntryWithShow(
                    entry = TrendingShowEntry(
                        id = id,
                        showId = show_id,
                        page = page,
                        watchers = watchers,
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
