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
import app.tivi.data.instantInPast
import app.tivi.inject.Tmdb
import app.tivi.inject.Trakt
import app.tivi.trakt.TraktAuthState
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class SeasonsEpisodesRepository @Inject constructor(
    private val dispatchers: AppCoroutineDispatchers,
    private val localEpisodeWatchStore: LocalEpisodeWatchStore,
    private val localSeasonsEpisodesStore: LocalSeasonsEpisodesStore,
    @Trakt private val traktSeasonsDataSource: SeasonsEpisodesDataSource,
    @Trakt private val traktEpisodeDataSource: EpisodeDataSource,
    @Tmdb private val tmdbEpisodeDataSource: EpisodeDataSource,
    private val traktAuthState: Provider<TraktAuthState>
) {
    fun observeSeasonsForShow(showId: Long) = localSeasonsEpisodesStore.observeShowSeasonsWithEpisodes(showId)

    fun observeEpisode(episodeId: Long) = localSeasonsEpisodesStore.observeEpisode(episodeId)

    fun observeEpisodeWatches(episodeId: Long) = localEpisodeWatchStore.observeEpisodeWatches(episodeId)

    suspend fun needShowSeasonsUpdate(showId: Long, expiry: Instant = instantInPast(days = 7)): Boolean {
        return localSeasonsEpisodesStore.lastShowSeasonsFetchBefore(showId, expiry)
    }

    suspend fun removeShowSeasonData(showId: Long) {
        localSeasonsEpisodesStore.deleteShowSeasonData(showId)
        localSeasonsEpisodesStore.updateShowEpisodeWatchesLastRequest(showId, Instant.now())
    }

    suspend fun updateSeasonsEpisodes(showId: Long) {
        val result = traktSeasonsDataSource.getSeasonsEpisodes(showId)
        when (result) {
            is Success -> {
                result.data.distinctBy { it.first.number }.associate { (season, episodes) ->
                    val localSeason = localSeasonsEpisodesStore.getSeasonWithTraktId(season.traktId!!)
                            ?: Season(showId = showId)
                    val mergedSeason = mergeSeason(localSeason, season, Season.EMPTY)

                    val mergedEpisodes = episodes.distinctBy(Episode::number).map {
                        val localEpisode = localSeasonsEpisodesStore.getEpisodeWithTraktId(it.traktId!!)
                                ?: Episode(seasonId = mergedSeason.id)
                        mergeEpisode(localEpisode, it, Episode.EMPTY)
                    }
                    mergedSeason to mergedEpisodes
                }.also { localSeasonsEpisodesStore.save(showId, it) }
            }
        }
        if (result is Success) {
            localSeasonsEpisodesStore.updateShowSeasonsFetchLastRequest(showId, Instant.now())
        }
    }

    suspend fun updateEpisode(episodeId: Long) = coroutineScope {
        val local = localSeasonsEpisodesStore.getEpisode(episodeId)!!
        val season = localSeasonsEpisodesStore.getSeason(local.seasonId)!!
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

        localSeasonsEpisodesStore.save(mergeEpisode(local, trakt, tmdb))
    }

    suspend fun updateShowEpisodeWatches(showId: Long) {
        if (traktAuthState.get() == TraktAuthState.LOGGED_IN) {
            val latestEntry = localEpisodeWatchStore.getLatestEntryWatchDateTimeForShow(showId)
            if (latestEntry != null) {
                fetchNewShowWatchesFromRemote(showId, latestEntry)
            } else {
                fetchShowWatchesFromRemote(showId)
            }
        }
    }

    suspend fun syncEpisodeWatchesForShow(showId: Long) {
        // Process any pending deletes
        localEpisodeWatchStore.getEntriesWithDeleteAction(showId).also {
            it.isNotEmpty() && processPendingDeletes(it)
        }

        // Process any pending adds
        localEpisodeWatchStore.getEntriesWithAddAction(showId).also {
            it.isNotEmpty() && processPendingAdditions(it)
        }

        if (traktAuthState.get() == TraktAuthState.LOGGED_IN) {
            fetchShowWatchesFromRemote(showId)
            localEpisodeWatchStore.updateShowEpisodeWatchesLastRequest(showId, Instant.now())
        }
    }

    suspend fun needShowEpisodeWatchesSync(showId: Long, expiry: Instant = instantInPast(hours = 1)): Boolean {
        return localSeasonsEpisodesStore.lastShowSeasonsFetchBefore(showId, expiry)
    }

    suspend fun markSeasonWatched(seasonId: Long, onlyAired: Boolean, date: ActionDate) {
        val watchesToSave = localSeasonsEpisodesStore.getEpisodesInSeason(seasonId).mapNotNull { episode ->
            if (!onlyAired || episode.firstAired?.isBefore(OffsetDateTime.now()) == true) {
                if (!localEpisodeWatchStore.hasEpisodeBeenWatched(episode.id)) {
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
            localEpisodeWatchStore.save(watchesToSave)
        }

        // Should probably make this more granular
        val season = localSeasonsEpisodesStore.getSeason(seasonId)!!
        syncEpisodeWatchesForShow(season.showId)
    }

    suspend fun markSeasonUnwatched(seasonId: Long) {
        val season = localSeasonsEpisodesStore.getSeason(seasonId)!!

        val watches = ArrayList<EpisodeWatchEntry>()
        localSeasonsEpisodesStore.getEpisodesInSeason(seasonId).forEach { episode ->
            watches += localEpisodeWatchStore.getWatchesForEpisode(episode.id)
        }
        if (watches.isNotEmpty()) {
            localEpisodeWatchStore.updateWatchEntriesWithAction(watches.map { it.id }, PendingAction.DELETE)
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
        localEpisodeWatchStore.save(entry)

        syncEpisodeWatches(episodeId)
    }

    suspend fun markEpisodeUnwatched(episodeId: Long) {
        val watchesForEpisode = localEpisodeWatchStore.getWatchesForEpisode(episodeId)
        if (watchesForEpisode.isNotEmpty()) {
            val ids = watchesForEpisode.map { it.id }
            // First mark them as pending deletion
            localEpisodeWatchStore.updateWatchEntriesWithAction(ids, PendingAction.DELETE)
        }
        syncEpisodeWatches(episodeId)
    }

    suspend fun removeEpisodeWatch(episodeWatchId: Long) {
        val episodeWatch = localEpisodeWatchStore.getEpisodeWatch(episodeWatchId)
        if (episodeWatch != null && episodeWatch.pendingAction != PendingAction.DELETE) {
            localEpisodeWatchStore.save(episodeWatch.copy(pendingAction = PendingAction.DELETE))
            syncEpisodeWatches(episodeWatch.episodeId)
        }
    }

    private suspend fun syncEpisodeWatches(episodeId: Long) {
        val watches = localEpisodeWatchStore.getWatchesForEpisode(episodeId)
        var needUpdate = false

        // Process any deletes first
        val toDelete = watches.filter { it.pendingAction == PendingAction.DELETE }
        if (toDelete.isNotEmpty() && processPendingDeletes(toDelete)) {
            needUpdate = true
        }

        // Process any uploads
        val toAdd = watches.filter { it.pendingAction == PendingAction.UPLOAD }
        if (toAdd.isNotEmpty() && processPendingAdditions(toAdd)) {
            needUpdate = true
        }

        if (needUpdate && traktAuthState.get() == TraktAuthState.LOGGED_IN) {
            fetchEpisodeWatchesFromRemote(episodeId)
        }
    }

    private suspend fun fetchNewShowWatchesFromRemote(showId: Long, since: OffsetDateTime) {
        when (val response = traktSeasonsDataSource.getShowEpisodeWatches(showId, since)) {
            is Success -> {
                val watches = response.data.mapNotNull { (episode, watchEntry) ->
                    // Grab the episode id if it exists
                    localSeasonsEpisodesStore.getEpisodeIdForTraktId(episode.traktId!!)?.let {
                        watchEntry.copy(episodeId = it)
                    }
                }
                localEpisodeWatchStore.save(watches)
            }
        }
    }

    private suspend fun fetchShowWatchesFromRemote(showId: Long) {
        when (val response = traktSeasonsDataSource.getShowEpisodeWatches(showId, null)) {
            is Success -> {
                val watches = response.data.mapNotNull { (episode, watchEntry) ->
                    // Grab the episode id if it exists
                    localSeasonsEpisodesStore.getEpisodeIdForTraktId(episode.traktId!!)?.let {
                        watchEntry.copy(episodeId = it)
                    }
                }
                localEpisodeWatchStore.syncShowWatchEntries(showId, watches)
            }
        }
    }

    private suspend fun fetchEpisodeWatchesFromRemote(episodeId: Long) {
        when (val response = traktSeasonsDataSource.getEpisodeWatches(episodeId, null)) {
            is Success -> {
                val watches = response.data.map { it.copy(episodeId = episodeId) }
                localEpisodeWatchStore.syncEpisodeWatchEntries(episodeId, watches)
            }
        }
    }

    /**
     * Process any pending episode watch deletes.
     *
     * @return true if a network service was updated
     */
    private suspend fun processPendingDeletes(entries: List<EpisodeWatchEntry>): Boolean {
        if (traktAuthState.get() == TraktAuthState.LOGGED_IN) {
            val response = traktSeasonsDataSource.removeEpisodeWatches(entries)
            if (response is Success) {
                // Now update the database
                localEpisodeWatchStore.deleteWatchEntriesWithIds(entries.map { it.id })
                return true
            }
        } else {
            // We're not logged in so just update the database
            localEpisodeWatchStore.deleteWatchEntriesWithIds(entries.map { it.id })
        }
        return false
    }

    /**
     * Process any pending episode watch adds.
     *
     * @return true if a network service was updated
     */
    private suspend fun processPendingAdditions(entries: List<EpisodeWatchEntry>): Boolean {
        if (traktAuthState.get() == TraktAuthState.LOGGED_IN) {
            val response = traktSeasonsDataSource.addEpisodeWatches(entries)
            if (response is Success) {
                // Now update the database
                localEpisodeWatchStore.updateWatchEntriesWithAction(entries.map { it.id }, PendingAction.NOTHING)
                return true
            }
        } else {
            // We're not logged in so just update the database
            localEpisodeWatchStore.updateWatchEntriesWithAction(entries.map { it.id }, PendingAction.NOTHING)
        }
        return false
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