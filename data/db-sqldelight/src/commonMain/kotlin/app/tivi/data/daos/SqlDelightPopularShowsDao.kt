// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.daos

import app.cash.paging.PagingSource
import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.paging3.QueryPagingSource
import app.tivi.data.Database
import app.tivi.data.compoundmodels.PopularEntryWithShow
import app.tivi.data.models.PopularShowEntry
import app.tivi.data.models.TiviShow
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
class SqlDelightPopularShowsDao(
    override val db: Database,
    private val dispatchers: AppCoroutineDispatchers,
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

    override fun deletePage(page: Int) {
        db.popular_showsQueries.deletePage(page)
    }

    override fun deleteAll() {
        db.popular_showsQueries.deleteAll()
    }

    override fun getLastPage(): Int? {
        return db.popular_showsQueries.getLastPage().executeAsOne().MAX?.toInt()
    }

    override fun deleteEntity(entity: PopularShowEntry) {
        db.popular_showsQueries.delete(entity.id)
    }

    override fun insert(entity: PopularShowEntry): Long {
        db.popular_showsQueries.insert(
            id = entity.id,
            show_id = entity.showId,
            page = entity.page,
            page_order = entity.pageOrder,
        )
        return db.popular_showsQueries.lastInsertRowId().executeAsOne()
    }

    override fun update(entity: PopularShowEntry) {
        db.popular_showsQueries.update(
            id = entity.id,
            show_id = entity.showId,
            page = entity.page,
            page_order = entity.pageOrder,
        )
    }

    private fun entriesWithShow(limit: Long, offset: Long): Query<PopularEntryWithShow> {
        return db.popular_showsQueries.entriesWithShow(
            limit = limit,
            offset = offset,
            mapper = {
                    id, show_id, page, page_order, id_, title, original_title, trakt_id, tmdb_id,
                    imdb_id, overview, homepage, trakt_rating, trakt_votes, certification,
                    first_aired, country, network, network_logo_path, runtime, genres,
                    status, airs_day, airs_time, airs_tz,
                ->
                PopularEntryWithShow(
                    entry = PopularShowEntry(
                        id = id,
                        showId = show_id,
                        page = page,
                        pageOrder = page_order,
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
