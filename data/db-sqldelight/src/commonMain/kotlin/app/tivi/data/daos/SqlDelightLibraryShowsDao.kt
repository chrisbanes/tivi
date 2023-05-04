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
import app.cash.sqldelight.paging3.QueryPagingSource
import app.tivi.data.Database
import app.tivi.data.compoundmodels.LibraryShow
import app.tivi.data.models.SortOption
import app.tivi.data.models.TiviShow
import app.tivi.data.models.WatchedShowEntry
import app.tivi.data.views.ShowsWatchStats
import app.tivi.util.AppCoroutineDispatchers
import me.tatarka.inject.annotations.Inject

@Inject
class SqlDelightLibraryShowsDao(
    private val db: Database,
    private val dispatchers: AppCoroutineDispatchers,
) : LibraryShowsDao {
    override fun pagedListLastWatched(
        sort: SortOption,
        includeWatched: Boolean,
        includeFollowed: Boolean,
    ): PagingSource<Int, LibraryShow> = internalPagedListLastWatched(
        sort = sort,
        filter = null,
        includeWatched = includeWatched,
        includeFollowed = includeFollowed,
    )

    override fun pagedListLastWatchedFilter(
        sort: SortOption,
        filter: String,
        includeWatched: Boolean,
        includeFollowed: Boolean,
    ): PagingSource<Int, LibraryShow> = internalPagedListLastWatched(
        sort = sort,
        filter = filter,
        includeWatched = includeWatched,
        includeFollowed = includeFollowed,
    )

    private fun internalPagedListLastWatched(
        sort: SortOption,
        filter: String?,
        includeWatched: Boolean,
        includeFollowed: Boolean,
    ): PagingSource<Int, LibraryShow> = QueryPagingSource(
        countQuery = db.library_showsQueries.count(
            includeWatched = if (includeWatched) 1 else 0,
            includeFollowed = if (includeFollowed) 1 else 0,
            filter = filter,
        ),
        transacter = db.library_showsQueries,
        context = dispatchers.io,
    ) { limit: Long, offset: Long ->
        db.library_showsQueries.entries(
            includeWatched = if (includeWatched) 1L else 0L,
            includeFollowed = if (includeFollowed) 1L else 0L,
            filter = filter,
            sort = sort.sqlValue,
            limit = limit,
            offset = offset,
        ) {
                id, title, original_title, trakt_id, tmdb_id, imdb_id, overview, homepage,
                trakt_rating, trakt_votes, certification, first_aired, country, network,
                network_logo_path, runtime, genres, status, airs_day, airs_time, airs_tz,
                // watched show
                id_, show_id, last_watched, last_updated,
                // show stats
                show_id_, episode_count, watched_episode_count,
            ->
            LibraryShow(
                show = TiviShow(
                    id, title, original_title, trakt_id, tmdb_id, imdb_id, overview, homepage,
                    trakt_rating, trakt_votes, certification, first_aired, country, network,
                    network_logo_path, runtime, genres, status, airs_day, airs_time, airs_tz,
                ),
                stats = show_id_?.let {
                    ShowsWatchStats(show_id_, episode_count!!, watched_episode_count!!)
                },
                watchedEntry = id_?.let {
                    WatchedShowEntry(id_, show_id!!, last_watched!!, last_updated!!)
                },
            )
        }
    }
}
