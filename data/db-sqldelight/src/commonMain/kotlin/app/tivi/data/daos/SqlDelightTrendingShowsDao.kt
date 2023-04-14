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
import app.tivi.data.await
import app.tivi.data.compoundmodels.TrendingEntryWithShow
import app.tivi.data.models.TiviShow
import app.tivi.data.models.TrendingShowEntry
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class SqlDelightTrendingShowsDao(
    private val db: Database,
    private val dispatchers: AppCoroutineDispatchers,
) : TrendingDao {
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

    override suspend fun deletePage(page: Int) = withContext(dispatchers.io) {
        db.trending_showsQueries.deletePage(page)
    }

    override suspend fun deleteAll() = withContext(dispatchers.io) {
        db.trending_showsQueries.deleteAll()
    }

    override suspend fun getLastPage(): Int? {
        return db.trending_showsQueries.getLastPage().await(dispatchers.io).MAX?.toInt()
    }

    override suspend fun upsert(entity: TrendingShowEntry): Long = withContext(dispatchers.io) {
        db.transactionWithResult {
            upsertBlocking(entity)
            db.trending_showsQueries.lastInsertRowId().executeAsOne()
        }
    }

    override suspend fun upsertAll(entities: List<TrendingShowEntry>) = withContext(dispatchers.io) {
        db.transaction {
            entities.forEach(::upsertBlocking)
        }
    }

    override suspend fun deleteEntity(entity: TrendingShowEntry) = withContext(dispatchers.io) {
        db.trending_showsQueries.delete(entity.id)
    }

    private fun upsertBlocking(entity: TrendingShowEntry) {
        db.trending_showsQueries.upsertShow(
            id = entity.id,
            show_id = entity.showId,
            page = entity.page,
            watchers = entity.watchers,
        )
    }

    private fun entriesWithShow(count: Long, offset: Long): Query<TrendingEntryWithShow> {
        return db.trending_showsQueries.entriesWithShow(
            count = count,
            offset = offset,
            mapper = { id, show_id, page, watchers, id_, title, original_title,
                    trakt_id, tmdb_id, imdb_id, overview, homepage, trakt_rating, trakt_votes,
                    certification, first_aired, country, network, network_logo_path, runtime, genres,
                    status, airs_day, airs_time, airs_tz, ->

                val show = TiviShow(
                    id_, title, original_title, trakt_id, tmdb_id, imdb_id, overview, homepage,
                    trakt_rating, trakt_votes, certification, first_aired, country, network,
                    network_logo_path, runtime, genres, status, airs_day, airs_time, airs_tz,
                )

                TrendingEntryWithShow().apply {
                    this.entry = TrendingShowEntry(
                        id = id,
                        showId = show_id,
                        page = page,
                        watchers = watchers,
                    )
                    this.relations = listOf(show)
                }
            },
        )
    }
}
