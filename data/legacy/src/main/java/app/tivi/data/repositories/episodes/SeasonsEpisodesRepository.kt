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

import app.tivi.data.DatabaseTransactionRunner
import app.tivi.data.daos.EpisodesDao
import app.tivi.data.daos.SeasonsDao
import app.tivi.data.entities.ActionDate
import app.tivi.data.entities.Episode
import app.tivi.data.entities.EpisodeWatchEntry
import app.tivi.data.entities.PendingAction
import app.tivi.data.entities.RefreshType
import app.tivi.data.entities.Season
import app.tivi.data.instantInPast
import app.tivi.data.resultentities.EpisodeWithSeason
import app.tivi.data.resultentities.SeasonWithEpisodesAndWatches
import app.tivi.data.syncers.syncerForEntity
import app.tivi.inject.Tmdb
import app.tivi.inject.Trakt
import app.tivi.trakt.TraktAuthState
import app.tivi.util.Logger
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import org.threeten.bp.Instant
import org.threeten.bp.OffsetDateTime

@Singleton
class SeasonsEpisodesRepository @Inject constructor(
    private val episodeWatchStore: EpisodeWatchStore,
    private val episodeWatchLastLastRequestStore: EpisodeWatchLastRequestStore,
    private val episodeLastRequestStore: EpisodeLastRequestStore,
    private val transactionRunner: DatabaseTransactionRunner,
    private val seasonsDao: SeasonsDao,
    private val episodesDao: EpisodesDao,
    private val seasonsLastRequestStore: SeasonsLastRequestStore,
    private val traktSeasonsDataSource: SeasonsEpisodesDataSource,
    @Trakt private val traktEpisodeDataSource: EpisodeDataSource,
    @Tmdb private val tmdbEpisodeDataSource: EpisodeDataSource,
    private val traktAuthState: Provider<TraktAuthState>,
    logger: Logger,
) {
    private val seasonSyncer = syncerForEntity(
        entityDao = seasonsDao,
        entityToKey = { it.traktId },
        mapper = { entity, id -> entity.copy(id = id ?: 0) },
        logger = logger,
    )

    private val episodeSyncer = syncerForEntity(
        entityDao = episodesDao,
        entityToKey = { it.traktId },
        mapper = { entity, id -> entity.copy(id = id ?: 0) },
        logger = logger,
    )

    fun observeSeasonsForShow(showId: Long): Flow<List<Season>> {
        return seasonsDao.observeSeasonsForShowId(showId)
    }

    fun observeSeasonsWithEpisodesWatchedForShow(showId: Long): Flow<List<SeasonWithEpisodesAndWatches>> {
        return seasonsDao.seasonsWithEpisodesForShowId(showId)
    }

    fun observeSeason(seasonId: Long): Flow<SeasonWithEpisodesAndWatches> {
        return seasonsDao.seasonWithEpisodes(seasonId)
    }

    fun observeEpisode(episodeId: Long): Flow<EpisodeWithSeason> {
        return episodesDao.episodeWithIdObservable(episodeId)
    }

    suspend fun getEpisode(episodeId: Long): Episode? {
        return episodesDao.episodeWithId(episodeId)
    }

    fun observeEpisodeWatches(episodeId: Long): Flow<List<EpisodeWatchEntry>> {
        return episodeWatchStore.observeEpisodeWatches(episodeId)
    }

    fun observeNextEpisodeToWatch(showId: Long): Flow<EpisodeWithSeason?> {
        return episodesDao.observeLatestWatchedEpisodeForShowId(showId).flatMapLatest {
            episodesDao.observeNextAiredEpisodeForShowAfter(
                showId,
                it?.season?.number ?: 0,
                it?.episode?.number ?: 0,
            )
        }
    }

    suspend fun needShowSeasonsUpdate(
        showId: Long,
        expiry: Instant = instantInPast(days = 7),
    ): Boolean {
        return seasonsLastRequestStore.isRequestBefore(showId, expiry)
    }

    suspend fun removeShowSeasonData(showId: Long) {
        seasonsDao.deleteSeasonsForShowId(showId)
    }

    suspend fun updateSeasonsEpisodes(showId: Long) {
        val response = traktSeasonsDataSource.getSeasonsEpisodes(showId)
        response.distinctBy { it.first.number }.associate { (season, episodes) ->
            val localSeason = seasonsDao.seasonWithTraktId(season.traktId!!)
                ?: Season(showId = showId)
            val mergedSeason = mergeSeason(localSeason, season, Season.EMPTY)

            val mergedEpisodes = episodes.distinctBy(Episode::number).map {
                val localEpisode = episodesDao.episodeWithTraktId(it.traktId!!)
                    ?: Episode(seasonId = mergedSeason.id)
                mergeEpisode(localEpisode, it, Episode.EMPTY)
            }
            mergedSeason to mergedEpisodes
        }.also { season ->
            transactionRunner {
                seasonSyncer.sync(seasonsDao.seasonsForShowId(showId), season.keys)
                season.forEach { (season, episodes) ->
                    val seasonId = seasonsDao.seasonWithTraktId(season.traktId!!)!!.id
                    val updatedEpisodes = episodes.map {
                        if (it.seasonId != seasonId) it.copy(seasonId = seasonId) else it
                    }
                    episodeSyncer.sync(episodesDao.episodesWithSeasonId(seasonId), updatedEpisodes)
                }
            }
        }

        seasonsLastRequestStore.updateLastRequest(showId)
    }

    suspend fun needEpisodeUpdate(
        episodeId: Long,
        expiry: Instant = instantInPast(days = 28),
    ): Boolean {
        return episodeLastRequestStore.isRequestBefore(episodeId, expiry)
    }

    suspend fun updateEpisode(episodeId: Long) = coroutineScope {
        val local = episodesDao.episodeWithId(episodeId)!!
        val season = seasonsDao.seasonWithId(local.seasonId)!!
        val traktDeferred = async {
            traktEpisodeDataSource.getEpisode(season.showId, season.number!!, local.number!!)
        }
        val tmdbDeferred = async {
            tmdbEpisodeDataSource.getEpisode(season.showId, season.number!!, local.number!!)
        }

        val trakt = try {
            traktDeferred.await()
        } catch (e: Exception) {
            null
        }
        val tmdb = try {
            tmdbDeferred.await()
        } catch (e: Exception) {
            null
        }
        check(trakt != null || tmdb != null)

        episodesDao.insertOrUpdate(
            mergeEpisode(local, trakt ?: Episode.EMPTY, tmdb ?: Episode.EMPTY),
        )
    }

    suspend fun updateShowEpisodeWatches(
        showId: Long,
        refreshType: RefreshType = RefreshType.QUICK,
        forceRefresh: Boolean = false,
        lastUpdated: OffsetDateTime? = null,
    ) {
        if (refreshType == RefreshType.QUICK) {
            // If we have a lastUpdated time and we've already fetched the watched episodes, we can try
            // and do a delta fetch
            if (lastUpdated != null && episodeWatchLastLastRequestStore.hasBeenRequested(showId)) {
                if (forceRefresh || needShowEpisodeWatchesSync(showId, lastUpdated.toInstant())) {
                    updateShowEpisodeWatches(showId, lastUpdated.plusSeconds(1))
                }
            } else {
                // We don't have a trakt date/time to use as a delta, so we'll do a full refresh.
                // If the user hasn't watched the show, this should be empty anyway
                if (forceRefresh || needShowEpisodeWatchesSync(showId)) {
                    updateShowEpisodeWatches(showId)
                }
            }
        } else if (refreshType == RefreshType.FULL) {
            // A full refresh is requested, so we pull down all history
            if (forceRefresh || needShowEpisodeWatchesSync(showId)) {
                updateShowEpisodeWatches(showId)
            }
        }
    }

    private suspend fun updateShowEpisodeWatches(showId: Long, since: OffsetDateTime? = null) {
        if (traktAuthState.get() == TraktAuthState.LOGGED_IN) {
            fetchShowWatchesFromRemote(showId, since)
        }
    }

    suspend fun syncEpisodeWatchesForShow(showId: Long) {
        // Process any pending deletes
        episodeWatchStore.getEntriesWithDeleteAction(showId).also {
            it.isNotEmpty() && processPendingDeletes(it)
        }

        // Process any pending adds
        episodeWatchStore.getEntriesWithAddAction(showId).also {
            it.isNotEmpty() && processPendingAdditions(it)
        }

        if (traktAuthState.get() == TraktAuthState.LOGGED_IN) {
            fetchShowWatchesFromRemote(showId)
        }
    }

    suspend fun needShowEpisodeWatchesSync(
        showId: Long,
        expiry: Instant = instantInPast(hours = 1),
    ): Boolean {
        return episodeWatchLastLastRequestStore.isRequestBefore(showId, expiry)
    }

    suspend fun markSeasonWatched(seasonId: Long, onlyAired: Boolean, date: ActionDate) {
        val watchesToSave = episodesDao.episodesWithSeasonId(seasonId).mapNotNull { episode ->
            if (!onlyAired || episode.firstAired?.isBefore(OffsetDateTime.now()) == true) {
                if (!episodeWatchStore.hasEpisodeBeenWatched(episode.id)) {
                    val timestamp = when (date) {
                        ActionDate.NOW -> OffsetDateTime.now()
                        ActionDate.AIR_DATE -> episode.firstAired ?: OffsetDateTime.now()
                    }
                    return@mapNotNull EpisodeWatchEntry(
                        episodeId = episode.id,
                        watchedAt = timestamp,
                        pendingAction = PendingAction.UPLOAD,
                    )
                }
            }
            null
        }

        if (watchesToSave.isNotEmpty()) {
            episodeWatchStore.save(watchesToSave)
        }

        // Should probably make this more granular
        val season = seasonsDao.seasonWithId(seasonId)!!
        syncEpisodeWatchesForShow(season.showId)
    }

    suspend fun markSeasonUnwatched(seasonId: Long) {
        val season = seasonsDao.seasonWithId(seasonId)!!

        val watches = ArrayList<EpisodeWatchEntry>()
        episodesDao.episodesWithSeasonId(seasonId).forEach { episode ->
            watches += episodeWatchStore.getWatchesForEpisode(episode.id)
        }
        if (watches.isNotEmpty()) {
            episodeWatchStore.updateEntriesWithAction(watches.map { it.id }, PendingAction.DELETE)
        }

        // Should probably make this more granular
        syncEpisodeWatchesForShow(season.showId)
    }

    suspend fun markSeasonFollowed(seasonId: Long) {
        seasonsDao.updateSeasonIgnoreFlag(seasonId, false)
    }

    suspend fun markSeasonIgnored(seasonId: Long) {
        seasonsDao.updateSeasonIgnoreFlag(seasonId, true)
    }

    suspend fun markPreviousSeasonsIgnored(seasonId: Long) {
        transactionRunner {
            for (id in seasonsDao.showPreviousSeasonIds(seasonId)) {
                seasonsDao.updateSeasonIgnoreFlag(id, true)
            }
        }
    }

    suspend fun addEpisodeWatch(episodeId: Long, timestamp: OffsetDateTime) {
        val entry = EpisodeWatchEntry(
            episodeId = episodeId,
            watchedAt = timestamp,
            pendingAction = PendingAction.UPLOAD,
        )
        episodeWatchStore.save(entry)

        syncEpisodeWatches(episodeId)
    }

    suspend fun removeEpisodeWatch(episodeWatchId: Long) {
        val episodeWatch = episodeWatchStore.getEpisodeWatch(episodeWatchId)
        if (episodeWatch != null && episodeWatch.pendingAction != PendingAction.DELETE) {
            episodeWatchStore.save(episodeWatch.copy(pendingAction = PendingAction.DELETE))
            syncEpisodeWatches(episodeWatch.episodeId)
        }
    }

    suspend fun removeAllEpisodeWatches(episodeId: Long) {
        val watchesForEpisode = episodeWatchStore.getWatchesForEpisode(episodeId)
        if (watchesForEpisode.isNotEmpty()) {
            // First mark them as pending deletion
            episodeWatchStore.updateEntriesWithAction(
                watchesForEpisode.map { it.id },
                PendingAction.DELETE,
            )
            syncEpisodeWatches(episodeId)
        }
    }

    private suspend fun syncEpisodeWatches(episodeId: Long) {
        val watches = episodeWatchStore.getWatchesForEpisode(episodeId)
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

    private suspend fun fetchShowWatchesFromRemote(showId: Long, since: OffsetDateTime? = null) {
        val response = traktSeasonsDataSource.getShowEpisodeWatches(showId, since)

        val watches = response.mapNotNull { (episode, watchEntry) ->
            val epId = episodesDao.episodeIdWithTraktId(episode.traktId!!)
                ?: return@mapNotNull null // We don't have the episode, skip
            watchEntry.copy(episodeId = epId)
        }
        if (since != null) {
            // We did a delta fetch, so just append/update the new watches
            if (watches.isNotEmpty()) {
                episodeWatchStore.addNewShowWatchEntries(showId, watches)
            }
        } else {
            episodeWatchStore.syncShowWatchEntries(showId, watches)
        }
        episodeWatchLastLastRequestStore.updateLastRequest(showId)
    }

    private suspend fun fetchEpisodeWatchesFromRemote(episodeId: Long) {
        val response = traktSeasonsDataSource.getEpisodeWatches(episodeId, null)
        val watches = response.map { it.copy(episodeId = episodeId) }
        episodeWatchStore.syncEpisodeWatchEntries(episodeId, watches)
    }

    /**
     * Process any pending episode watch deletes.
     *
     * @return true if a network service was updated
     */
    private suspend fun processPendingDeletes(entries: List<EpisodeWatchEntry>): Boolean {
        if (traktAuthState.get() == TraktAuthState.LOGGED_IN) {
            val localOnlyDeletes = entries.filter { it.traktId == null }
            // If we've got deletes which are local only, just remove them from the DB
            if (localOnlyDeletes.isNotEmpty()) {
                episodeWatchStore.deleteEntriesWithIds(localOnlyDeletes.map(EpisodeWatchEntry::id))
            }

            if (entries.size > localOnlyDeletes.size) {
                val toRemove = entries.filter { it.traktId != null }
                traktSeasonsDataSource.removeEpisodeWatches(toRemove)
                // Now update the database
                episodeWatchStore.deleteEntriesWithIds(entries.map(EpisodeWatchEntry::id))
                return true
            }
        } else {
            // We're not logged in so just update the database
            episodeWatchStore.deleteEntriesWithIds(entries.map { it.id })
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
            traktSeasonsDataSource.addEpisodeWatches(entries)
            // Now update the database
            episodeWatchStore.updateEntriesWithAction(entries.map { it.id }, PendingAction.NOTHING)
            return true
        } else {
            // We're not logged in so just update the database
            episodeWatchStore.updateEntriesWithAction(entries.map { it.id }, PendingAction.NOTHING)
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
        tmdbBackdropPath = tmdb.tmdbBackdropPath ?: local.tmdbBackdropPath,
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
        tmdbBackdropPath = tmdb.tmdbBackdropPath ?: local.tmdbBackdropPath,
    )
}
