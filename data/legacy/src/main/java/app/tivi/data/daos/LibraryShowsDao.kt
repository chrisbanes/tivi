/*
 * Copyright 2017 Google LLC
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

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import app.tivi.data.models.Season
import app.tivi.data.models.SortOption
import app.tivi.data.models.TiviShow
import app.tivi.data.resultentities.LibraryShow

@Dao
abstract class LibraryShowsDao : EntityDao<TiviShow>() {

    fun observeForPaging(
        sort: SortOption,
        filter: String?,
        includeWatched: Boolean,
        includeFollowed: Boolean,
    ): PagingSource<Int, LibraryShow> = if (filter.isNullOrEmpty()) {
        pagedListLastWatched(sort, includeWatched, includeFollowed)
    } else {
        pagedListLastWatchedFilter(sort, "*$filter*", includeWatched, includeFollowed)
    }

    @Transaction
    @Query(QUERY_LAST_WATCHED)
    internal abstract fun pagedListLastWatched(
        sort: SortOption,
        includeWatched: Boolean,
        includeFollowed: Boolean,
    ): PagingSource<Int, LibraryShow>

    @Transaction
    @Query(QUERY_LAST_WATCHED_FILTER)
    internal abstract fun pagedListLastWatchedFilter(
        sort: SortOption,
        filter: String,
        includeWatched: Boolean,
        includeFollowed: Boolean,
    ): PagingSource<Int, LibraryShow>

    companion object {
        private const val QUERY_LAST_WATCHED = """
            SELECT shows.* FROM shows
            LEFT JOIN myshows_entries ON shows.id = myshows_entries.show_id
            LEFT JOIN watched_entries ON shows.id = watched_entries.show_id
            LEFT JOIN seasons AS s ON shows.id = s.show_id
            LEFT JOIN episodes AS eps ON eps.season_id = s.id
            LEFT JOIN episode_watch_entries as ew ON ew.episode_id = eps.id
            WHERE
                (s.number IS NULL OR s.number != ${Season.NUMBER_SPECIALS})
                AND (
                    (:includeWatched = 1 AND watched_entries.id IS NOT NULL) OR
                    (:includeFollowed = 1 AND myshows_entries.id IS NOT NULL)
                )
            GROUP BY shows.id
            ORDER BY CASE
                WHEN :sort = 'last_watched' THEN
                    (CASE WHEN MAX(datetime(watched_entries.last_watched)) IS NULL
                    OR MAX(datetime(ew.watched_at)) > MAX(datetime(watched_entries.last_watched))
                    THEN MAX(datetime(ew.watched_at))
                    ELSE MAX(datetime(watched_entries.last_watched)) END)
                END DESC,
                shows.title ASC
        """

        private const val QUERY_LAST_WATCHED_FILTER = """
            SELECT shows.* FROM shows
            INNER JOIN shows_fts ON shows.id = shows_fts.docid
            LEFT JOIN myshows_entries ON shows.id = myshows_entries.show_id
            LEFT JOIN watched_entries ON shows.id = watched_entries.show_id
            LEFT JOIN seasons AS s ON shows.id = s.show_id
            LEFT JOIN episodes AS eps ON eps.season_id = s.id
            LEFT JOIN episode_watch_entries as ew ON ew.episode_id = eps.id
            WHERE
                (s.number IS NULL OR s.number != ${Season.NUMBER_SPECIALS})
                AND (
                    (:includeWatched = 1 AND watched_entries.id IS NOT NULL) OR
                    (:includeFollowed = 1 AND myshows_entries.id IS NOT NULL)
                )
                AND shows_fts.title MATCH :filter
            GROUP BY shows.id
            ORDER BY CASE
                WHEN :sort = 'last_watched' THEN
                    (CASE WHEN MAX(datetime(watched_entries.last_watched)) IS NULL
                    OR MAX(datetime(ew.watched_at)) > MAX(datetime(watched_entries.last_watched))
                    THEN MAX(datetime(ew.watched_at))
                    ELSE MAX(datetime(watched_entries.last_watched)) END)
                END DESC,
                shows.title ASC
        """
    }
}
