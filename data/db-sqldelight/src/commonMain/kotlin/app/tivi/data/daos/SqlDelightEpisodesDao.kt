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

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import app.tivi.data.Database
import app.tivi.data.await
import app.tivi.data.awaitAsNull
import app.tivi.data.awaitList
import app.tivi.data.compoundmodels.EpisodeWithSeason
import app.tivi.data.models.Episode
import app.tivi.data.upsert
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class SqlDelightEpisodesDao(
    override val db: Database,
    override val dispatchers: AppCoroutineDispatchers,
    private val seasonsDao: SeasonsDao,
) : EpisodesDao, SqlDelightEntityDao<Episode> {
    override fun upsertBlocking(entity: Episode): Long = db.episodesQueries.upsert(
        entity = entity,
        insert = { entity ->
            insert(
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
        },
        update = { entity ->
            update(
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
        },
        lastInsertRowId = { lastInsertRowId().executeAsOne() },
    )

    override suspend fun episodesWithSeasonId(seasonId: Long): List<Episode> {
        return db.episodesQueries.episodesWithSeasonId(seasonId, ::Episode)
            .awaitList(dispatchers.io)
    }

    override suspend fun deleteWithSeasonId(seasonId: Long) {
        withContext(dispatchers.io) {
            db.episodesQueries.deleteWithSeasonId(seasonId)
        }
    }

    override suspend fun episodeWithTraktId(traktId: Int): Episode? {
        return db.episodesQueries.episodeWithTraktId(traktId, ::Episode)
            .awaitAsNull(dispatchers.io)
    }

    override suspend fun episodeWithTmdbId(tmdbId: Int): Episode? {
        return db.episodesQueries.episodeWithTmdbId(tmdbId, ::Episode)
            .awaitAsNull(dispatchers.io)
    }

    override suspend fun episodeWithId(id: Long): Episode? {
        return db.episodesQueries.episodeWithId(id, ::Episode)
            .awaitAsNull(dispatchers.io)
    }

    override suspend fun episodeTraktIdForId(id: Long): Int? {
        return db.episodesQueries.traktIdForId(id)
            .awaitAsNull(dispatchers.io)
            ?.trakt_id
    }

    override suspend fun episodeIdWithTraktId(traktId: Int): Long? {
        return db.episodesQueries.idForTraktId(traktId)
            .awaitAsNull(dispatchers.io)
    }

    override fun episodeWithIdObservable(id: Long): Flow<EpisodeWithSeason> {
        return db.episodesQueries.episodeWithId(id, ::Episode)
            .asFlow()
            .mapToOne(dispatchers.io)
            .flatMapWithSeason()
    }

    override suspend fun showIdForEpisodeId(episodeId: Long): Long {
        return db.episodesQueries.showIdForEpisodeId(episodeId)
            .await(dispatchers.io)
    }

    override fun observeLatestWatchedEpisodeForShowId(showId: Long): Flow<EpisodeWithSeason?> {
        return db.episodesQueries.latestWatchedEpisodeForShowId(showId, ::Episode)
            .asFlow()
            .mapToOneOrNull(dispatchers.io)
            .flatMapWithSeasonNullable()
    }

    override fun observeNextEpisodeForShowAfter(
        showId: Long,
        seasonNumber: Int,
        episodeNumber: Int,
    ): Flow<EpisodeWithSeason?> = db.episodesQueries.nextEpisodeForShowIdAfter(
        showId = showId,
        seasonNumber = seasonNumber.toLong(),
        episodeNumber = episodeNumber.toLong(),
        mapper = ::Episode,
    )
        .asFlow()
        .mapToOneOrNull(dispatchers.io)
        .flatMapWithSeasonNullable()

    override fun observeNextAiredEpisodeForShowAfter(
        showId: Long,
        seasonNumber: Int,
        episodeNumber: Int,
    ): Flow<EpisodeWithSeason?> = db.episodesQueries.nextAiredEpisodeForShowIdAfter(
        showId = showId,
        seasonNumber = seasonNumber.toLong(),
        episodeNumber = episodeNumber.toLong(),
        mapper = ::Episode,
    )
        .asFlow()
        .mapToOneOrNull(dispatchers.io)
        .flatMapWithSeasonNullable()

    override suspend fun deleteEntity(entity: Episode) = withContext(dispatchers.io) {
        db.episodesQueries.delete(entity.id)
    }

    private fun Flow<Episode>.flatMapWithSeason(): Flow<EpisodeWithSeason> {
        return flatMapLatest { episode ->
            seasonsDao.observeSeasonWithId(episode.seasonId).map { season ->
                EpisodeWithSeason().apply {
                    this.episode = episode
                    _seasons = listOf(season)
                }
            }
        }
    }

    private fun Flow<Episode?>.flatMapWithSeasonNullable(): Flow<EpisodeWithSeason?> {
        return flatMapLatest { episode ->
            if (episode == null) {
                emptyFlow()
            } else {
                seasonsDao.observeSeasonWithId(episode.seasonId).map { season ->
                    EpisodeWithSeason().apply {
                        this.episode = episode
                        _seasons = listOf(season)
                    }
                }
            }
        }
    }
}
