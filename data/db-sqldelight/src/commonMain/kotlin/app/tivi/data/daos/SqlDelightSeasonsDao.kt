// Copyright 2023, Christopher Banes and the Tivi project contributors
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
import kotlinx.coroutines.flow.map
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class SqlDelightSeasonsDao(
  override val db: Database,
  private val dispatchers: AppCoroutineDispatchers,
) : SeasonsDao,
  SqlDelightEntityDao<Season> {

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
    return db.seasonsQueries.seasonsWithEpisodesWithWatchesForShowId(showId)
      .asFlow()
      .mapToList(dispatchers.io)
      .map { items ->
        val seasons = items
          .asSequence()
          .distinctBy { it.id }
          .map {
            Season(
              id = it.id,
              showId = it.show_id,
              traktId = it.trakt_id,
              tmdbId = it.tmdb_id,
              title = it.title,
              summary = it.overview,
              number = it.number,
              network = it.network,
              episodeCount = it.ep_count,
              episodesAired = it.ep_aired,
              traktRating = it.trakt_rating,
              traktRatingVotes = it.trakt_votes,
              tmdbPosterPath = it.tmdb_poster_path,
              tmdbBackdropPath = it.tmdb_backdrop_path,
              ignored = it.ignored,
            )
          }
          .toList()
          .sortedBy { season ->
            when (val number = season.number) {
              Season.NUMBER_SPECIALS -> Int.MAX_VALUE
              else -> number
            }
          }

        val episodes = items
          .asSequence()
          .distinctBy { it.id_ }
          .map {
            Episode(
              id = it.id_,
              seasonId = it.season_id,
              traktId = it.trakt_id_,
              tmdbId = it.tmdb_id_,
              title = it.title_,
              summary = it.overview_,
              number = it.number_,
              firstAired = it.first_aired,
              traktRating = it.trakt_rating_,
              traktRatingVotes = it.trakt_rating_votes,
              tmdbBackdropPath = it.tmdb_backdrop_path_,
            )
          }

        val watches = items
          .asSequence()
          .filter { it.id__ != null }
          .mapNotNull {
            if (it.id__ != null) {
              EpisodeWatchEntry(
                id = it.id__,
                episodeId = it.episode_id!!,
                traktId = it.trakt_id__,
                watchedAt = it.watched_at!!,
                pendingAction = it.pending_action!!,
              )
            } else {
              null
            }
          }
          .toList()

        seasons.map { season ->
          val seasonEps = episodes
            .filter { it.seasonId == season.id }
            .toList()
            .sortedBy { it.number }

          SeasonWithEpisodesAndWatches(
            season = season,
            episodes = seasonEps.map { episode ->
              EpisodeWithWatches(
                episode = episode,
                watches = watches.filter { it.episodeId == episode.id },
              )
            },
          )
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
