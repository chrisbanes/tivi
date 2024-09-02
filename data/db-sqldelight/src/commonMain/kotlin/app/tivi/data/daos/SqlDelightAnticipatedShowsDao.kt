// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.daos

import androidx.paging.PagingSource
import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.tivi.data.Database
import app.tivi.data.compoundmodels.AnticipatedShowEntryWithShow
import app.tivi.data.models.AnticipatedShowEntry
import app.tivi.data.models.TiviShow
import app.tivi.data.paging.QueryPagingSource
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
class SqlDelightAnticipatedShowsDao(
  override val db: Database,
  private val dispatchers: AppCoroutineDispatchers,
) : AnticipatedShowsDao,
  SqlDelightEntityDao<AnticipatedShowEntry> {
  override fun entriesObservable(page: Int): Flow<List<AnticipatedShowEntry>> {
    return db.anticipated_showsQueries.entriesInPage(page, ::AnticipatedShowEntry)
      .asFlow()
      .mapToList(dispatchers.io)
  }

  override fun entriesObservable(count: Int, offset: Int): Flow<List<AnticipatedShowEntryWithShow>> {
    return entriesWithShow(count.toLong(), offset.toLong()).asFlow().mapToList(dispatchers.io)
  }

  override fun entriesPagingSource(): PagingSource<Int, AnticipatedShowEntryWithShow> {
    return QueryPagingSource(
      countQuery = db.anticipated_showsQueries.count(),
      transacter = db.anticipated_showsQueries,
      context = dispatchers.io,
      queryProvider = ::entriesWithShow,
    )
  }

  override fun deletePage(page: Int) {
    db.anticipated_showsQueries.deletePage(page)
  }

  override fun deleteAll() {
    db.anticipated_showsQueries.deleteAll()
  }

  override fun getLastPage(): Int? {
    return db.anticipated_showsQueries.getLastPage().executeAsOne().MAX?.toInt()
  }

  override fun deleteEntity(entity: AnticipatedShowEntry) {
    db.anticipated_showsQueries.delete(entity.id)
  }

  override fun insert(entity: AnticipatedShowEntry): Long {
    db.anticipated_showsQueries.insert(
      id = entity.id,
      show_id = entity.showId,
      page = entity.page,
      page_order = entity.pageOrder,
    )
    return db.anticipated_showsQueries.lastInsertRowId().executeAsOne()
  }

  override fun update(entity: AnticipatedShowEntry) {
    db.anticipated_showsQueries.update(
      id = entity.id,
      show_id = entity.showId,
      page = entity.page,
      page_order = entity.pageOrder,
    )
  }

  private fun entriesWithShow(limit: Long, offset: Long): Query<AnticipatedShowEntryWithShow> {
    return db.anticipated_showsQueries.entriesWithShow(
      limit = limit,
      offset = offset,
      mapper = {
          id,
          show_id,
          page,
          page_order,
          id_,
          title,
          original_title,
          trakt_id,
          tmdb_id,
          imdb_id,
          overview,
          homepage,
          trakt_rating,
          trakt_votes,
          certification,
          first_aired,
          country,
          network,
          network_logo_path,
          runtime,
          genres,
          status,
          airs_day,
          airs_time,
          airs_tz,
        ->
        AnticipatedShowEntryWithShow(
          entry = AnticipatedShowEntry(
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
