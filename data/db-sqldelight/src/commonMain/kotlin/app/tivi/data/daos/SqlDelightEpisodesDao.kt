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
import app.tivi.data.compoundmodels.EpisodeWithSeason
import app.tivi.data.models.Episode
import app.tivi.data.models.Season
import app.tivi.data.upsert
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject

@Inject
class SqlDelightEpisodesDao(
    override val db: Database,
    private val dispatchers: AppCoroutineDispatchers,
) : EpisodesDao, SqlDelightEntityDao<Episode> {
    override fun upsert(entity: Episode): Long = db.episodesQueries.upsert(
        entity = entity,
        insert = {
            insert(
                id = it.id,
                season_id = it.seasonId,
                trakt_id = it.traktId,
                tmdb_id = it.tmdbId,
                title = it.title,
                overview = it.summary,
                number = it.number,
                first_aired = it.firstAired,
                trakt_rating = it.traktRating,
                trakt_rating_votes = it.traktRatingVotes,
                tmdb_backdrop_path = it.tmdbBackdropPath,
            )
        },
        update = {
            update(
                id = it.id,
                season_id = it.seasonId,
                trakt_id = it.traktId,
                tmdb_id = it.tmdbId,
                title = it.title,
                overview = it.summary,
                number = it.number,
                first_aired = it.firstAired,
                trakt_rating = it.traktRating,
                trakt_rating_votes = it.traktRatingVotes,
                tmdb_backdrop_path = it.tmdbBackdropPath,
            )
        },
        lastInsertRowId = { lastInsertRowId().executeAsOne() },
    )

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
