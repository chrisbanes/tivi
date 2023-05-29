// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.daos

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.tivi.data.Database
import app.tivi.data.compoundmodels.EpisodeWithWatches
import app.tivi.data.compoundmodels.SeasonWithEpisodesAndWatches
import app.tivi.data.models.Episode
import app.tivi.data.models.EpisodeWatchEntry
import app.tivi.data.models.Season
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class SqlDelightSeasonsDao(
    override val db: Database,
    private val dispatchers: AppCoroutineDispatchers,
) : SeasonsDao, SqlDelightEntityDao<Season> {

    override fun insert(entity: Season): Long {
        db.seasonsQueries.insert(
            id = entity.id,
            show_id = entity.showId,
            trakt_id = entity.traktId,
            tmdb_id = entity.tmdbId,
            title = entity.title,
            overview = entity.summary,
            number = entity.number,
            network = entity.network,
            ep_count = entity.episodeCount,
            ep_aired = entity.episodesAired,
            trakt_rating = entity.traktRating,
            trakt_votes = entity.traktRatingVotes,
            tmdb_poster_path = entity.tmdbPosterPath,
            tmdb_backdrop_path = entity.tmdbBackdropPath,
            ignored = entity.ignored,
        )
        return db.seasonsQueries.lastInsertRowId().executeAsOne()
    }

    override fun update(entity: Season) {
        db.seasonsQueries.update(
            id = entity.id,
            show_id = entity.showId,
            trakt_id = entity.traktId,
            tmdb_id = entity.tmdbId,
            title = entity.title,
            overview = entity.summary,
            number = entity.number,
            network = entity.network,
            ep_count = entity.episodeCount,
            ep_aired = entity.episodesAired,
            trakt_rating = entity.traktRating,
            trakt_votes = entity.traktRatingVotes,
            tmdb_poster_path = entity.tmdbPosterPath,
            tmdb_backdrop_path = entity.tmdbBackdropPath,
            ignored = entity.ignored,
        )
    }

    override fun seasonsWithEpisodesForShowId(showId: Long): Flow<List<SeasonWithEpisodesAndWatches>> {
        return db.seasonsQueries.seasonsForShowId(showId, ::Season)
            .asFlow()
            .mapToList(dispatchers.io)
            // This should be flatMapLatest
            .flatMapLatest { seasons ->
                val flows = seasons.map { season ->
                    db.episodesQueries.episodesWithSeasonId(season.id, ::Episode)
                        .asFlow()
                        .mapToList(dispatchers.io)
                        .map { episodes -> season to episodes }
                        .flatMapLatest { (season, episodes) ->
                            val watchFlows = episodes.map { episode ->
                                db.episode_watch_entriesQueries.watchesForEpisodeId(
                                    episodeId = episode.id,
                                    mapper = ::EpisodeWatchEntry,
                                )
                                    .asFlow()
                                    .mapToList(dispatchers.io)
                                    .map { watches -> episode to watches }
                            }

                            combine(watchFlows) { combined ->
                                combined.map { (episode, watches) ->
                                    EpisodeWithWatches(
                                        episode = episode,
                                        watches = watches,
                                    )
                                }
                            }.map { episodesWithWatches ->
                                SeasonWithEpisodesAndWatches(
                                    season = season,
                                    episodes = episodesWithWatches,
                                )
                            }
                        }
                }
                combine(flows) { combined ->
                    combined.map { it }
                }
            }
    }

    override fun observeSeasonWithId(id: Long): Flow<Season> {
        return db.seasonsQueries.seasonWithId(id, ::Season)
            .asFlow()
            .mapToOne(dispatchers.io)
    }

    override fun seasonsForShowId(showId: Long): List<Season> {
        return db.seasonsQueries.seasonsForShowId(showId, ::Season).executeAsList()
    }

    override fun deleteWithShowId(showId: Long) {
        db.seasonsQueries.deleteWithShowId(showId)
    }

    override fun seasonWithId(id: Long): Season? {
        return db.seasonsQueries.seasonWithId(id, ::Season).executeAsOneOrNull()
    }

    override fun traktIdForId(id: Long): Int? {
        return db.seasonsQueries.traktIdForId(id).executeAsOneOrNull()?.trakt_id
    }

    override fun seasonWithTraktId(traktId: Int): Season? {
        return db.seasonsQueries.seasonWithTraktId(traktId, ::Season)
            .executeAsOneOrNull()
    }

    override fun showPreviousSeasonIds(seasonId: Long): List<Long> {
        return db.seasonsQueries.previousSeasonsForShowId(seasonId)
            .executeAsList()
    }

    override fun updateSeasonIgnoreFlag(
        seasonId: Long,
        ignored: Boolean,
    ) {
        db.seasonsQueries.updateSeasonIgnored(ignored = ignored, seasonId = seasonId)
    }

    override fun seasonWithShowIdAndNumber(showId: Long, number: Int): Season? {
        return db.seasonsQueries.seasonForShowIdAndNumber(showId, number, ::Season)
            .executeAsOneOrNull()
    }

    override fun deleteEntity(entity: Season) {
        db.seasonsQueries.deleteWithId(entity.id)
    }
}
