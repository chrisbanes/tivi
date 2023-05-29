// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.daos

import app.cash.paging.PagingSource
import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import app.cash.sqldelight.paging3.QueryPagingSource
import app.tivi.data.Database
import app.tivi.data.compoundmodels.UpNextEntry
import app.tivi.data.models.Episode
import app.tivi.data.models.Season
import app.tivi.data.models.SortOption
import app.tivi.data.models.TiviShow
import app.tivi.data.models.WatchedShowEntry
import app.tivi.data.sqlValue
import app.tivi.data.views.ShowsWatchStats
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
class SqlDelightWatchedShowsDao(
    override val db: Database,
    private val dispatchers: AppCoroutineDispatchers,
) : WatchedShowDao, SqlDelightEntityDao<WatchedShowEntry> {
    override fun entryWithShowId(showId: Long): WatchedShowEntry? {
        return db.watched_entriesQueries.entryWithShowId(showId, ::WatchedShowEntry)
            .executeAsOneOrNull()
    }

    override fun entries(): List<WatchedShowEntry> {
        return db.watched_entriesQueries.entries(::WatchedShowEntry)
            .executeAsList()
    }

    override fun entriesObservable(): Flow<List<WatchedShowEntry>> {
        return db.watched_entriesQueries.entries(::WatchedShowEntry)
            .asFlow()
            .mapToList(dispatchers.io)
    }

    override fun deleteAll() {
        db.watched_entriesQueries.deleteAll()
    }

    override fun pagedUpNextShows(
        followedOnly: Boolean,
        sort: SortOption,
    ): PagingSource<Int, UpNextEntry> = QueryPagingSource(
        countQuery = db.upnext_showsQueries.upNextShowsCount(followedOnly.sqlValue),
        transacter = db.watched_entriesQueries,
        context = dispatchers.io,
        queryProvider = { count, offset ->
            provideUpNextShowsQuery(
                followedOnly = followedOnly,
                sort = sort,
                limit = count,
                offset = offset,
            )
        },
    )

    override fun getUpNextShows(): List<UpNextEntry> {
        return provideUpNextShowsQuery(
            followedOnly = false,
            sort = SortOption.LAST_WATCHED,
            limit = Long.MAX_VALUE,
            offset = 0,
        ).executeAsList()
    }

    override fun entryShowViewStats(showId: Long): Flow<ShowsWatchStats?> {
        return db.shows_view_watch_statsQueries.watchStatsForShowId(showId, ::ShowsWatchStats)
            .asFlow()
            .mapToOneOrNull(dispatchers.io)
    }

    override fun observeNextShowToWatch(): Flow<TiviShow?> {
        return db.shows_next_to_watchQueries.nextShowToWatch(::TiviShow)
            .asFlow()
            .mapToOneOrNull(dispatchers.io)
    }

    override fun insert(entity: WatchedShowEntry): Long {
        db.watched_entriesQueries.insert(
            id = entity.id,
            show_id = entity.showId,
            last_watched = entity.lastWatched,
            last_updated = entity.lastUpdated,
        )
        return db.watched_entriesQueries.lastInsertRowId().executeAsOne()
    }

    override fun update(entity: WatchedShowEntry) {
        db.watched_entriesQueries.update(
            id = entity.id,
            show_id = entity.showId,
            last_watched = entity.lastWatched,
            last_updated = entity.lastUpdated,
        )
    }

    override fun deleteEntity(entity: WatchedShowEntry) {
        db.watched_entriesQueries.delete(entity.id)
    }

    private fun provideUpNextShowsQuery(
        followedOnly: Boolean,
        sort: SortOption,
        limit: Long,
        offset: Long,
    ): Query<UpNextEntry> = db.upnext_showsQueries.upNextShows(
        followedOnly = followedOnly.sqlValue,
        sort = sort.sqlValue,
        limit = limit,
        offset = offset,
    ) {
            // show
            id, title, original_title, trakt_id, tmdb_id, imdb_id, overview, homepage, trakt_rating,
            trakt_votes, certification, first_aired, country, network, network_logo_path, runtime,
            genres, status, airs_day, airs_time, airs_tz,
            // season
            id_, show_id, trakt_id_, tmdb_id_, title_, overview_, number, network_, ep_count,
            ep_aired, trakt_rating_, trakt_votes_, tmdb_poster_path, tmdb_backdrop_path, ignored,
            // episode
            id__, season_id, trakt_id__, tmdb_id__, title__, overview__, number_, first_aired_,
            trakt_rating__, trakt_rating_votes, tmdb_backdrop_path_,
        ->

        val show = TiviShow(
            id = id,
            title = title,
            originalTitle = original_title,
            traktId = trakt_id,
            tmdbId = tmdb_id,
            imdbId = imdb_id,
            summary = overview,
            homepage = homepage,
            traktRating = trakt_rating,
            traktVotes = trakt_votes,
            certification = certification,
            firstAired = first_aired,
            country = country,
            network = network,
            networkLogoPath = network_logo_path,
            runtime = runtime,
            _genres = genres,
            status = status,
            airsDay = airs_day,
            airsTime = airs_time,
            airsTimeZone = airs_tz,
        )

        val season = Season(
            id = id_,
            showId = show_id,
            traktId = trakt_id_,
            tmdbId = tmdb_id_,
            title = title_,
            summary = overview_,
            number = number,
            network = network_,
            episodeCount = ep_count,
            episodesAired = ep_aired,
            traktRating = trakt_rating_,
            traktRatingVotes = trakt_votes_,
            tmdbPosterPath = tmdb_poster_path,
            tmdbBackdropPath = tmdb_backdrop_path,
            ignored = ignored,
        )

        val episode = Episode(
            id = id__,
            seasonId = season_id,
            traktId = trakt_id__,
            tmdbId = tmdb_id__,
            title = title__,
            summary = overview__,
            number = number_,
            firstAired = first_aired_,
            traktRating = trakt_rating__,
            traktRatingVotes = trakt_rating_votes,
            tmdbBackdropPath = tmdb_backdrop_path_,
        )

        UpNextEntry(
            show = show,
            season = season,
            episode = episode,
        )
    }
}
