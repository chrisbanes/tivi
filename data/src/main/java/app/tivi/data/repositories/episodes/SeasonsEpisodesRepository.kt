/*
 * Copyright 2018 Google, Inc.
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

package app.tivi.data.repositories.episodes

import app.tivi.data.entities.Episode
import app.tivi.data.entities.Season
import javax.inject.Inject

class SeasonsEpisodesRepository @Inject constructor(
    private val localStore: LocalSeasonsEpisodesStore,
    private val traktDataSource: TraktSeasonsEpisodesDataSource,
    private val tmdbDataSource: TmdbSeasonsEpisodesDataSource
) {
    fun observeSeasonsForShow(showId: Long) = localStore.observeShowSeasonsWithEpisodes(showId)

    fun observeEpisode(episodeId: Long) = localStore.observeEpisode(episodeId)

    suspend fun updateSeasonsEpisodes(showId: Long) {
        traktDataSource.getSeasonsEpisodes(showId)
                .map { (traktSeason, episodes) ->
                    val localSeason = localStore.getSeasonWithTraktId(traktSeason.traktId!!)
                            ?: Season(showId = showId)
                    val mergedSeason = mergeSeason(localSeason, traktSeason, Season.EMPTY)

                    val mergedEpisodes = episodes.map {
                        val localEpisode = localStore.getEpisodeWithTraktId(it.traktId!!)
                                ?: Episode(seasonId = mergedSeason.showId)
                        mergeEpisode(localEpisode, it, Episode.EMPTY)
                    }
                    (mergedSeason to mergedEpisodes)
                }
                .also {
                    // Save the seasons + episodes
                    localStore.save(it)
                }
    }

    suspend fun updateEpisode(episodeId: Long) {
        val local = localStore.getEpisode(episodeId)!!
        val season = localStore.getSeason(local.seasonId)!!

        // TODO move these to async()s to be concurrent
        val trakt = traktDataSource.getEpisode(season.showId, season.number!!, local.number!!) ?: Episode.EMPTY
        val tmdb = tmdbDataSource.getEpisode(season.showId, season.number, local.number) ?: Episode.EMPTY

        localStore.save(mergeEpisode(local, trakt, tmdb))
    }

    private fun mergeSeason(local: Season, trakt: Season, tmdb: Season) = local.copy(
            title = trakt.title ?: local.title,
            summary = trakt.summary ?: local.summary,
            number = trakt.number ?: local.number,

            network = trakt.network ?: tmdb.network ?: local.network,
            episodeCount = trakt.episodeCount ?: tmdb.episodeCount ?: local.episodeCount,
            episodesAired = trakt.episodesAired ?: tmdb.episodesAired ?: local.episodesAired,

            // Trakt specific stuff
            traktId = trakt.traktId ?: local.traktId,
            traktRating = trakt.traktRating ?: local.traktRating,
            traktRatingVotes = trakt.traktRatingVotes ?: local.traktRatingVotes,

            // TMDb specific stuff
            tmdbId = tmdb.tmdbId ?: trakt.tmdbId ?: local.tmdbId,
            tmdbPosterPath = tmdb.tmdbPosterPath ?: local.tmdbPosterPath,
            tmdbBackdropPath = tmdb.tmdbBackdropPath ?: local.tmdbBackdropPath
    )

    private fun mergeEpisode(local: Episode, trakt: Episode, tmdb: Episode) = local.copy(
            title = trakt.title ?: local.title,
            summary = trakt.summary ?: local.summary,
            number = trakt.number ?: local.number,
            firstAired = trakt.firstAired ?: local.firstAired,

            // Trakt specific stuff
            traktId = trakt.traktId ?: local.traktId,
            traktRating = trakt.traktRating ?: local.traktRating,
            traktRatingVotes = trakt.traktRatingVotes ?: local.traktRatingVotes,

            // TMDb specific stuff
            tmdbId = tmdb.tmdbId ?: trakt.tmdbId ?: local.tmdbId,
            tmdbBackdropPath = tmdb.tmdbBackdropPath ?: local.tmdbBackdropPath
    )
}