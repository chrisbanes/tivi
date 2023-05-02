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
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.tivi.data.Database
import app.tivi.data.awaitAsNull
import app.tivi.data.awaitList
import app.tivi.data.compoundmodels.EpisodeWithWatches
import app.tivi.data.compoundmodels.SeasonWithEpisodesAndWatches
import app.tivi.data.models.Episode
import app.tivi.data.models.EpisodeWatchEntry
import app.tivi.data.models.Season
import app.tivi.data.upsert
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@Inject
class SqlDelightSeasonsDao(
    override val db: Database,
    override val dispatchers: AppCoroutineDispatchers,
) : SeasonsDao, SqlDelightEntityDao<Season> {
    override fun upsertBlocking(entity: Season): Long = db.seasonsQueries.upsert(
        entity = entity,
        insert = { entity ->
            insert(
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
        },
        update = { entity ->
            update(
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
        },
        lastInsertRowId = { lastInsertRowId().executeAsOne() },
    )

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

    override suspend fun seasonsForShowId(showId: Long): List<Season> {
        return db.seasonsQueries.seasonsForShowId(showId, ::Season).awaitList(dispatchers.io)
    }

    override suspend fun deleteWithShowId(showId: Long): Unit = withContext(dispatchers.io) {
        db.seasonsQueries.deleteWithShowId(showId)
    }

    override suspend fun seasonWithId(id: Long): Season? {
        return db.seasonsQueries.seasonWithId(id, ::Season).awaitAsNull(dispatchers.io)
    }

    override suspend fun traktIdForId(id: Long): Int? {
        return db.seasonsQueries.traktIdForId(id).awaitAsNull(dispatchers.io)?.trakt_id
    }

    override suspend fun seasonWithTraktId(traktId: Int): Season? {
        return db.seasonsQueries.seasonWithTraktId(traktId, ::Season)
            .awaitAsNull(dispatchers.io)
    }

    override suspend fun showPreviousSeasonIds(seasonId: Long): LongArray {
        // TODO: Return a List instead
        return db.seasonsQueries.previousSeasonsForShowId(seasonId)
            .awaitList(dispatchers.io)
            .toLongArray()
    }

    override suspend fun updateSeasonIgnoreFlag(
        seasonId: Long,
        ignored: Boolean,
    ): Unit = withContext(dispatchers.io) {
        db.seasonsQueries.updateSeasonIgnored(ignored = ignored, seasonId = seasonId)
    }

    override suspend fun seasonWithShowIdAndNumber(showId: Long, number: Int): Season? {
        return db.seasonsQueries.seasonForShowIdAndNumber(showId, number, ::Season)
            .awaitAsNull(dispatchers.io)
    }

    override suspend fun deleteEntity(entity: Season) = withContext(dispatchers.io) {
        db.seasonsQueries.deleteWithId(entity.id)
    }
}
