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
import app.tivi.data.compoundmodels.PopularEntryWithShow
import app.tivi.data.models.PopularShowEntry
import app.tivi.data.models.TiviShow
import app.tivi.data.upsert
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class SqlDelightPopularShowsDao(
    override val db: Database,
    override val dispatchers: AppCoroutineDispatchers,
) : PopularDao, SqlDelightEntityDao<PopularShowEntry> {
    override fun entriesObservable(page: Int): Flow<List<PopularShowEntry>> {
        return db.popular_showsQueries.entriesInPage(page, ::PopularShowEntry)
            .asFlow()
            .mapToList(dispatchers.io)
    }

    override fun entriesObservable(count: Int, offset: Int): Flow<List<PopularEntryWithShow>> {
        return entriesWithShow(count.toLong(), offset.toLong()).asFlow().mapToList(dispatchers.io)
    }

    override fun entriesPagingSource(): PagingSource<Int, PopularEntryWithShow> {
        return QueryPagingSource(
            countQuery = db.popular_showsQueries.count(),
            transacter = db.popular_showsQueries,
            context = dispatchers.io,
            queryProvider = ::entriesWithShow,
        )
    }

    override suspend fun deletePage(page: Int) = withContext(dispatchers.io) {
        db.popular_showsQueries.deletePage(page)
    }

    override suspend fun deleteAll() = withContext(dispatchers.io) {
        db.popular_showsQueries.deleteAll()
    }

    override suspend fun getLastPage(): Int? {
        return db.popular_showsQueries.getLastPage().await(dispatchers.io).MAX?.toInt()
    }

    override suspend fun deleteEntity(entity: PopularShowEntry) = withContext(dispatchers.io) {
        db.popular_showsQueries.delete(entity.id)
    }

    override fun upsertBlocking(entity: PopularShowEntry): Long = db.popular_showsQueries.upsert(
        entity = entity,
        insert = { entry ->
            insert(
                id = entry.id,
                show_id = entry.showId,
                page = entry.page,
                page_order = entry.pageOrder,
            )
        },
        update = { entry ->
            update(
                id = entry.id,
                show_id = entry.showId,
                page = entry.page,
                page_order = entry.pageOrder,
            )
        },
        lastInsertRowId = { lastInsertRowId().executeAsOne() },
    )

    private fun entriesWithShow(limit: Long, offset: Long): Query<PopularEntryWithShow> {
        return db.popular_showsQueries.entriesWithShow(
            limit = limit,
            offset = offset,
            mapper = {
                    id, show_id, page, page_order, id_, title, original_title,
                    trakt_id, tmdb_id, imdb_id, overview, homepage, trakt_rating, trakt_votes,
                    certification, first_aired, country, network, network_logo_path, runtime, genres,
                    status, airs_day, airs_time, airs_tz, ->

                val show = TiviShow(
                    id_, title, original_title, trakt_id, tmdb_id, imdb_id, overview, homepage,
                    trakt_rating, trakt_votes, certification, first_aired, country, network,
                    network_logo_path, runtime, genres, status, airs_day, airs_time, airs_tz,
                )

                PopularEntryWithShow().apply {
                    this.entry = PopularShowEntry(
                        id = id,
                        showId = show_id,
                        page = page,
                        pageOrder = page_order,
                    )
                    this.relations = listOf(show)
                }
            },
        )
    }
}
