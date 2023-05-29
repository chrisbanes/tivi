// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.daos

import app.cash.paging.PagingSource
import app.cash.sqldelight.paging3.QueryPagingSource
import app.tivi.data.Database
import app.tivi.data.compoundmodels.LibraryShow
import app.tivi.data.models.SortOption
import app.tivi.data.models.TiviShow
import app.tivi.data.models.WatchedShowEntry
import app.tivi.data.sqlValue
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
        filter: String?,
        includeWatched: Boolean,
        includeFollowed: Boolean,
    ): PagingSource<Int, LibraryShow> {
        val searchQuery = when {
            filter.isNullOrEmpty() -> null
            else -> "%$filter%"
        }
        return QueryPagingSource(
            countQuery = db.library_showsQueries.count(
                includeWatched = includeWatched.sqlValue,
                includeFollowed = includeFollowed.sqlValue,
                filter = searchQuery,
            ),
            transacter = db.library_showsQueries,
            context = dispatchers.io,
        ) { limit: Long, offset: Long ->
            db.library_showsQueries.entries(
                includeWatched = includeWatched.sqlValue,
                includeFollowed = includeFollowed.sqlValue,
                filter = searchQuery,
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
}
