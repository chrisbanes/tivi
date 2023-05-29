// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.daos

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import app.tivi.data.Database
import app.tivi.data.compoundmodels.EpisodeWithSeason
import app.tivi.data.models.Episode
import app.tivi.data.models.Season
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject

@Inject
class SqlDelightEpisodesDao(
    override val db: Database,
    private val dispatchers: AppCoroutineDispatchers,
) : EpisodesDao, SqlDelightEntityDao<Episode> {

    override fun insert(entity: Episode): Long {
        db.episodesQueries.insert(
            id = entity.id,
            season_id = entity.seasonId,
            trakt_id = entity.traktId,
            tmdb_id = entity.tmdbId,
            title = entity.title,
            overview = entity.summary,
            number = entity.number,
            first_aired = entity.firstAired,
            trakt_rating = entity.traktRating,
            trakt_rating_votes = entity.traktRatingVotes,
            tmdb_backdrop_path = entity.tmdbBackdropPath,
        )
        return db.episodesQueries.lastInsertRowId().executeAsOne()
    }

    override fun update(entity: Episode) {
        db.episodesQueries.update(
            id = entity.id,
            season_id = entity.seasonId,
            trakt_id = entity.traktId,
            tmdb_id = entity.tmdbId,
            title = entity.title,
            overview = entity.summary,
            number = entity.number,
            first_aired = entity.firstAired,
            trakt_rating = entity.traktRating,
            trakt_rating_votes = entity.traktRatingVotes,
            tmdb_backdrop_path = entity.tmdbBackdropPath,
        )
    }

    override fun episodesWithSeasonId(seasonId: Long): List<Episode> {
        return db.episodesQueries.episodesWithSeasonId(seasonId, ::Episode)
            .executeAsList()
    }

    override fun deleteWithSeasonId(seasonId: Long) {
        db.episodesQueries.deleteWithSeasonId(seasonId)
    }

    override fun episodeWithTraktId(traktId: Int): Episode? {
        return db.episodesQueries.episodeWithTraktId(traktId, ::Episode)
            .executeAsOneOrNull()
    }

    override fun episodeWithTmdbId(tmdbId: Int): Episode? {
        return db.episodesQueries.episodeWithTmdbId(tmdbId, ::Episode)
            .executeAsOneOrNull()
    }

    override fun episodeWithId(id: Long): Episode? {
        return db.episodesQueries.episodeWithId(id, ::Episode)
            .executeAsOneOrNull()
    }

    override fun episodeTraktIdForId(id: Long): Int? {
        return db.episodesQueries.traktIdForId(id)
            .executeAsOneOrNull()
            ?.trakt_id
    }

    override fun episodeIdWithTraktId(traktId: Int): Long? {
        return db.episodesQueries.idForTraktId(traktId)
            .executeAsOneOrNull()
    }

    override fun episodeWithIdObservable(id: Long): Flow<EpisodeWithSeason> {
        return db.episodesQueries.episodeWithIdWithSeason(
            id = id,
            mapper = ::mapperForEpisodeWithSeason,
        )
            .asFlow()
            .mapToOne(dispatchers.io)
    }

    override fun showIdForEpisodeId(episodeId: Long): Long {
        return db.episodesQueries.showIdForEpisodeId(episodeId)
            .executeAsOne()
    }

    override fun observeNextEpisodeToWatch(showId: Long): Flow<EpisodeWithSeason?> {
        return db.episodesQueries.nextWatchedEpisodeForShowId(
            showId = showId,
            mapper = ::mapperForEpisodeWithSeason,
        )
            .asFlow()
            .mapToOneOrNull(dispatchers.io)
    }

    private fun mapperForEpisodeWithSeason(
        id: Long,
        season_id: Long,
        trakt_id: Int?,
        tmdb_id: Int?,
        title: String?,
        overview: String?,
        number: Int?,
        first_aired: Instant?,
        trakt_rating: Float?,
        trakt_rating_votes: Int?,
        tmdb_backdrop_path: String?,
        id_: Long,
        show_id: Long,
        trakt_id_: Int?,
        tmdb_id_: Int?,
        title_: String?,
        overview_: String?,
        number_: Int?,
        network: String?,
        ep_count: Int?,
        ep_aired: Int?,
        trakt_rating_: Float?,
        trakt_votes: Int?,
        tmdb_poster_path: String?,
        tmdb_backdrop_path_: String?,
        ignored: Boolean,
    ): EpisodeWithSeason = EpisodeWithSeason(
        episode = Episode(
            id, season_id, trakt_id, tmdb_id, title, overview, number, first_aired,
            trakt_rating, trakt_rating_votes, tmdb_backdrop_path,
        ),
        season = Season(
            id_, show_id, trakt_id_, tmdb_id_, title_, overview_, number_, network, ep_count,
            ep_aired, trakt_rating_, trakt_votes, tmdb_poster_path, tmdb_backdrop_path_,
            ignored,
        ),
    )

    override fun deleteEntity(entity: Episode) {
        db.episodesQueries.delete(entity.id)
    }
}
