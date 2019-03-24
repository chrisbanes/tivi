/*
 * Copyright 2018 Google LLC
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

import app.tivi.data.entities.ActionDate
import app.tivi.data.entities.Episode
import app.tivi.data.entities.EpisodeWatchEntry
import app.tivi.data.entities.PendingAction
import app.tivi.data.entities.Season
import app.tivi.data.entities.Success
import app.tivi.inject.Tmdb
import app.tivi.inject.Trakt
import app.tivi.trakt.TraktAuthState
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.threeten.bp.Duration
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.Period
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class SeasonsEpisodesRepository @Inject constructor(
    private val dispatchers: AppCoroutineDispatchers,
    private val localStore: LocalSeasonsEpisodesStore,
    @Trakt private val traktSeasonsDataSource: SeasonsEpisodesDataSource,
    @Trakt private val traktEpisodeDataSource: EpisodeDataSource,
    @Tmdb private val tmdbEpisodeDataSource: EpisodeDataSource,
    private val traktAuthState: Provider<TraktAuthState>
) {
    fun observeSeasonsForShow(showId: Long) = localStore.observeShowSeasonsWithEpisodes(showId)

    fun observeEpisode(episodeId: Long) = localStore.observeEpisode(episodeId)

    fun observeEpisodeWatches(episodeId: Long) = localStore.observeEpisodeWatches(episodeId)

    suspend fun needShowSeasonsUpdate(showId: Long): Boolean {
        return localStore.lastShowSeasonsFetchBefore(showId, Period.ofDays(7))
    }

    suspend fun removeShowSeasonData(showId: Long) = localStore.deleteShowSeasonData(showId)

    suspend fun updateSeasonsEpisodes(showId: Long) {
        val result = traktSeasonsDataSource.getSeasonsEpisodes(showId)
        when (result) {
            is Success -> {
                result.data.distinctBy { it.first.number }.associate { (season, episodes) ->
                    val localSeason = localStore.getSeasonWithTraktId(season.traktId!!)
                            ?: Season(showId = showId)
                    val mergedSeason = mergeSeason(localSeason, season, Season.EMPTY)

                    val mergedEpisodes = episodes.distinctBy(Episode::number).map {
                        val localEpisode = localStore.getEpisodeWithTraktId(it.traktId!!)
                                ?: Episode(seasonId = mergedSeason.id)
                        mergeEpisode(localEpisode, it, Episode.EMPTY)
                    }
                    mergedSeason to mergedEpisodes
                }.also { localStore.save(showId, it) }
            }
        }
        if (result is Success) {
            localStore.updateShowSeasonsFetchLastRequest(showId)
        }
    }

    suspend fun updateEpisode(episodeId: Long) = coroutineScope {
        val local = localStore.getEpisode(episodeId)!!
        val season = localStore.getSeason(local.seasonId)!!
        val traktResult = async(dispatchers.io) {
            traktEpisodeDataSource.getEpisode(season.showId, season.number!!, local.number!!)
        }
        val tmdbResult = async(dispatchers.io) {
            tmdbEpisodeDataSource.getEpisode(season.showId, season.number!!, local.number!!)
        }

        val trakt = traktResult.await().let {
            if (it is Success) it.data else Episode.EMPTY
        }
        val tmdb = tmdbResult.await().let {
            if (it is Success) it.data else Episode.EMPTY
        }

        localStore.save(mergeEpisode(local, trakt, tmdb))
    }

    suspend fun syncEpisodeWatchesForShow(showId: Long) {
        // Process any pending deletes
        localStore.getEntriesWithDeleteAction(showId).also {
            if (it.isNotEmpty()) {
                processPendingDeletes(it)
            }
        }

        // Process any pending adds
        localStore.getEntriesWithAddAction(showId).also {
            if (it.isNotEmpty()) {
                processPendingAdditions(it)
            }
        }

        if (traktAuthState.get() == TraktAuthState.LOGGED_IN) {
            refreshShowWatchesFromRemote(showId)
            localStore.updateShowEpisodeWatchesLastRequest(showId)
        }
    }

    suspend fun needShowEpisodeWatchesSync(showId: Long): Boolean {
        return localStore.lastShowEpisodeWatchesSyncBefore(showId, Duration.ofHours(1))
    }

    suspend fun markSeasonWatched(seasonId: Long, onlyAired: Boolean, date: ActionDate) {
        val watchesToSave = localStore.getEpisodesInSeason(seasonId).mapNotNull { episode ->
            if (!onlyAired || episode.firstAired?.isBefore(OffsetDateTime.now()) == true) {
                if (!localStore.hasEpisodeBeenWatched(episode.id)) {
                    val timestamp = when (date) {
                        ActionDate.NOW -> OffsetDateTime.now()
                        ActionDate.AIR_DATE -> episode.firstAired?.plusHours(1) ?: OffsetDateTime.now()
                    }
                    return@mapNotNull EpisodeWatchEntry(
                            episodeId = episode.id,
                            watchedAt = timestamp,
                            pendingAction = PendingAction.UPLOAD
                    )
                }
            }
            null
        }

        if (watchesToSave.isNotEmpty()) {
            localStore.saveWatches(watchesToSave)
        }

        // Should probably make this more granular
        val season = localStore.getSeason(seasonId)!!
        syncEpisodeWatchesForShow(season.showId)
    }

    suspend fun markSeasonUnwatched(seasonId: Long) {
        val season = localStore.getSeason(seasonId)!!

        val watches = ArrayList<EpisodeWatchEntry>()
        localStore.getEpisodesInSeason(seasonId).forEach { episode ->
            watches += localStore.getWatchesForEpisode(episode.id)
        }
        if (watches.isNotEmpty()) {
            localStore.updateWatchEntriesWithAction(watches.map { it.id }, PendingAction.DELETE)
        }

        // Should probably make this more granular
        syncEpisodeWatchesForShow(season.showId)
    }

    suspend fun markEpisodeWatched(episodeId: Long, timestamp: OffsetDateTime) {
        val entry = EpisodeWatchEntry(
                episodeId = episodeId,
                watchedAt = timestamp,
                pendingAction = PendingAction.UPLOAD
        )
        localStore.save(entry)

        syncEpisodeWatches(episodeId)
    }

    suspend fun markEpisodeUnwatched(episodeId: Long) {
        val watchesForEpisode = localStore.getWatchesForEpisode(episodeId)
        if (watchesForEpisode.isNotEmpty()) {
            val ids = watchesForEpisode.map { it.id }
            // First mark them as pending deletion
            localStore.updateWatchEntriesWithAction(ids, PendingAction.DELETE)
        }
        syncEpisodeWatches(episodeId)
    }

    suspend fun removeEpisodeWatch(episodeWatchId: Long) {
        val episodeWatch = localStore.getEpisodeWatch(episodeWatchId)
        if (episodeWatch != null && episodeWatch.pendingAction != PendingAction.DELETE) {
            localStore.save(episodeWatch.copy(pendingAction = PendingAction.DELETE))
            syncEpisodeWatches(episodeWatch.episodeId)
        }
    }

    private suspend fun syncEpisodeWatches(episodeId: Long) {
        val watches = localStore.getWatchesForEpisode(episodeId)

        // Process any deletes first
        watches.filter { it.pendingAction == PendingAction.DELETE }.also {
            if (it.isNotEmpty()) {
                processPendingDeletes(it)
            }
        }

        // Process any uploads
        watches.filter { it.pendingAction == PendingAction.UPLOAD }.also {
            if (it.isNotEmpty()) {
                processPendingAdditions(it)
            }
        }

        if (traktAuthState.get() == TraktAuthState.LOGGED_IN) {
            refreshEpisodeWatchesFromRemote(episodeId)
        }
    }

    private suspend fun refreshShowWatchesFromRemote(showId: Long) {
        val response = traktSeasonsDataSource.getShowEpisodeWatches(showId)

        when (response) {
            is Success -> {
                response.data.mapNotNull { (episode, watchEntry) ->
                    // Grab the episode id if it exists
                    localStore.getEpisodeIdForTraktId(episode.traktId!!)?.let {
                        watchEntry.copy(episodeId = it)
                    }
                }.also {
                    localStore.syncShowWatchEntries(showId, it)
                }
            }
        }
    }

    private suspend fun refreshEpisodeWatchesFromRemote(episodeId: Long) {
        val response = traktSeasonsDataSource.getEpisodeWatches(episodeId)
        when (response) {
            is Success -> {
                response.data.map {
                    it.copy(episodeId = episodeId)
                }.also {
                    localStore.syncEpisodeWatchEntries(episodeId, it)
                }
            }
        }
    }

    private suspend fun processPendingDeletes(entries: List<EpisodeWatchEntry>) {
        if (traktAuthState.get() == TraktAuthState.LOGGED_IN) {
            val response = traktSeasonsDataSource.removeEpisodeWatches(entries)
            if (response is Success) {
                // Now update the database
                localStore.deleteWatchEntriesWithIds(entries.map { it.id })
            }
        } else {
            // We're not logged in so just update the database
            localStore.deleteWatchEntriesWithIds(entries.map { it.id })
        }
    }

    private suspend fun processPendingAdditions(entries: List<EpisodeWatchEntry>) {
        if (traktAuthState.get() == TraktAuthState.LOGGED_IN) {
            val response = traktSeasonsDataSource.addEpisodeWatches(entries)
            if (response is Success) {
                // Now update the database
                localStore.updateWatchEntriesWithAction(entries.map { it.id }, PendingAction.NOTHING)
            }
        } else {
            // We're not logged in so just update the database
            localStore.updateWatchEntriesWithAction(entries.map { it.id }, PendingAction.NOTHING)
        }
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
            title = trakt.title ?: tmdb.title ?: local.title,
            summary = trakt.summary ?: tmdb.summary ?: local.summary,
            number = trakt.number ?: tmdb.number ?: local.number,
            firstAired = trakt.firstAired ?: tmdb.firstAired ?: local.firstAired,

            // Trakt specific stuff
            traktId = trakt.traktId ?: local.traktId,
            traktRating = trakt.traktRating ?: local.traktRating,
            traktRatingVotes = trakt.traktRatingVotes ?: local.traktRatingVotes,

            // TMDb specific stuff
            tmdbId = tmdb.tmdbId ?: trakt.tmdbId ?: local.tmdbId,
            tmdbBackdropPath = tmdb.tmdbBackdropPath ?: local.tmdbBackdropPath
    )
}