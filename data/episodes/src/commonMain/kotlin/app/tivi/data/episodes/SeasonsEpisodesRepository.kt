// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.episodes

import app.tivi.data.compoundmodels.EpisodeWithSeason
import app.tivi.data.compoundmodels.SeasonWithEpisodesAndWatches
import app.tivi.data.compoundmodels.ShowSeasonEpisode
import app.tivi.data.daos.EpisodesDao
import app.tivi.data.daos.SeasonsDao
import app.tivi.data.daos.TiviShowDao
import app.tivi.data.db.DatabaseTransactionRunner
import app.tivi.data.episodes.datasource.EpisodeWatchesDataSource
import app.tivi.data.models.ActionDate
import app.tivi.data.models.Episode
import app.tivi.data.models.EpisodeWatchEntry
import app.tivi.data.models.PendingAction
import app.tivi.data.models.Season
import app.tivi.data.traktauth.TraktAuthRepository
import app.tivi.data.traktauth.TraktAuthState
import app.tivi.data.util.inPast
import app.tivi.data.util.syncerForEntity
import app.tivi.inject.ApplicationScope
import app.tivi.util.Logger
import app.tivi.util.cancellableRunCatching
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject

@ApplicationScope
@Inject
class SeasonsEpisodesRepository(
  private val episodeWatchStore: EpisodeWatchStore,
  private val episodeWatchLastLastRequestStore: EpisodeWatchLastRequestStore,
  private val episodeLastRequestStore: EpisodeLastRequestStore,
  private val seasonLastRequestStore: SeasonLastRequestStore,
  private val transactionRunner: DatabaseTransactionRunner,
  private val seasonsDao: SeasonsDao,
  private val episodesDao: EpisodesDao,
  private val showDao: TiviShowDao,
  private val showSeasonsLastRequestStore: ShowSeasonsLastRequestStore,
  private val tmdbSeasonsDataSource: TmdbSeasonsEpisodesDataSource,
  private val traktSeasonsDataSource: TraktSeasonsEpisodesDataSource,
  private val traktEpisodeDataSource: TraktEpisodeDataSource,
  private val tmdbEpisodeDataSource: TmdbEpisodeDataSource,
  private val traktEpisodeWatchesDataSource: EpisodeWatchesDataSource,
  private val traktAuthRepository: TraktAuthRepository,
  private val logger: Logger,
) {
  private val seasonSyncer = syncerForEntity(
    entityDao = seasonsDao,
    entityToKey = { it.traktId },
    mapper = { newEntity, currentEntity -> newEntity.copy(id = currentEntity?.id ?: 0) },
    logger = logger,
  )

  private val episodeSyncer = syncerForEntity(
    entityDao = episodesDao,
    entityToKey = { it.traktId },
    mapper = { newEntity, currentEntity -> newEntity.copy(id = currentEntity?.id ?: 0) },
    logger = logger,
  )

  fun observeSeasonsWithEpisodesWatchedForShow(showId: Long): Flow<List<SeasonWithEpisodesAndWatches>> {
    return seasonsDao.seasonsWithEpisodesForShowId(showId)
  }

  fun observeEpisode(episodeId: Long): Flow<EpisodeWithSeason> {
    return episodesDao.episodeWithIdObservable(episodeId).filterNotNull()
  }

  fun getEpisode(episodeId: Long): Episode? {
    return episodesDao.episodeWithId(episodeId)
  }

  fun getSeason(seasonId: Long): Season? {
    return seasonsDao.seasonWithId(seasonId)
  }

  fun getUpcomingEpisodesFromFollowedShows(limit: Instant): List<ShowSeasonEpisode> {
    return episodesDao.upcomingEpisodesFromFollowedShows(limit)
      .mapNotNull { episode ->
        val season = seasonsDao.seasonWithId(episode.seasonId) ?: return@mapNotNull null
        val show = showDao.getShowWithId(season.showId) ?: return@mapNotNull null
        ShowSeasonEpisode(show, season, episode)
      }
  }

  fun observeEpisodeWatches(episodeId: Long): Flow<List<EpisodeWatchEntry>> {
    return episodeWatchStore.observeEpisodeWatches(episodeId)
  }

  fun observeNextEpisodeToWatch(showId: Long): Flow<EpisodeWithSeason?> {
    return episodesDao.observeNextEpisodeToWatch(showId)
      .distinctUntilChanged()
  }

  fun needShowSeasonsUpdate(
    showId: Long,
    expiry: Instant? = null,
  ): Boolean = showSeasonsLastRequestStore.isRequestBefore(
    entityId = showId,
    instant = expiry ?: 7.days.inPast,
  )

  suspend fun removeShowSeasonData(showId: Long) {
    seasonsDao.deleteWithShowId(showId)
  }

  suspend fun updateSeasonsEpisodes(showId: Long) = coroutineScope {
    val traktDeferred = async { traktSeasonsDataSource.getSeasonsEpisodes(showId) }
    val tmdbDeferred = async { tmdbSeasonsDataSource.getSeasonsEpisodes(showId) }

    val traktResult = cancellableRunCatching {
      traktDeferred.await().distinctBy { it.first.number }
    }.onFailure {
      logger.i(it) { "Error whilst fetching seasons from Trakt" }
    }.getOrDefault(emptyList())

    val tmdbResult = cancellableRunCatching {
      tmdbDeferred.await().distinctBy { it.first.number }
    }.onFailure {
      logger.i(it) { "Error whilst fetching seasons from TMDb" }
    }.getOrNull()

    traktResult.associate { (traktSeason, traktEpisodes) ->
      val localSeason = seasonsDao.seasonWithTraktId(traktSeason.traktId!!)
        ?: Season(showId = showId)

      val mergedSeason = mergeSeason(
        local = localSeason,
        trakt = traktSeason,
        tmdb = tmdbResult
          ?.firstOrNull { it.first.number == traktSeason.number }?.first
          ?: Season.EMPTY,
      )

      val mergedEpisodes = traktEpisodes.distinctBy(Episode::number).map {
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
    }.also {
      showSeasonsLastRequestStore.updateLastRequest(showId)
    }
  }

  suspend fun needEpisodeUpdate(
    episodeId: Long,
    expiry: Instant = 28.days.inPast,
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
      cancellableRunCatching {
        tmdbEpisodeDataSource.getEpisode(season.showId, season.number!!, local.number!!)
      }.getOrNull()
    }

    val trakt = cancellableRunCatching { traktDeferred.await() }.getOrNull()
    val tmdb = cancellableRunCatching { tmdbDeferred.await() }.getOrNull()

    check(trakt != null || tmdb != null)

    episodesDao.upsert(
      mergeEpisode(local, trakt ?: Episode.EMPTY, tmdb ?: Episode.EMPTY),
    )

    episodeLastRequestStore.updateLastRequest(episodeId)
  }

  fun needSeasonUpdate(
    seasonId: Long,
    expiry: Instant = 28.days.inPast,
  ): Boolean {
    return seasonLastRequestStore.isRequestBefore(seasonId, expiry)
  }

  suspend fun updateSeason(seasonId: Long) = coroutineScope {
    val local = seasonsDao.seasonWithId(seasonId) ?: Season.EMPTY
    val traktDeferred = async {
      traktSeasonsDataSource.getSeason(local.showId, local.number!!)
    }
    val tmdbDeferred = async {
      tmdbSeasonsDataSource.getSeason(local.showId, local.number!!)
    }

    val trakt = try {
      traktDeferred.await()
    } catch (ce: CancellationException) {
      throw ce
    } catch (e: Exception) {
      null
    }
    val tmdb = try {
      tmdbDeferred.await()
    } catch (ce: CancellationException) {
      throw ce
    } catch (e: Exception) {
      null
    }
    check(trakt != null || tmdb != null)

    seasonsDao.upsert(mergeSeason(local, trakt ?: Season.EMPTY, tmdb ?: Season.EMPTY))

    seasonLastRequestStore.updateLastRequest(seasonId)
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

    if (traktAuthRepository.state.value == TraktAuthState.LOGGED_IN) {
      updateShowEpisodeWatches(showId)
    }
  }

  suspend fun needShowEpisodeWatchesSync(
    showId: Long,
    expiry: Instant? = null,
  ): Boolean = episodeWatchLastLastRequestStore.isRequestBefore(
    entityId = showId,
    instant = expiry ?: 24.hours.inPast,
  )

  suspend fun markSeasonWatched(seasonId: Long, onlyAired: Boolean, date: ActionDate) {
    val watchesToSave = episodesDao.episodesWithSeasonId(seasonId).mapNotNull { episode ->
      if (!onlyAired || episode.hasAired) {
        if (!episodeWatchStore.hasEpisodeBeenWatched(episode.id)) {
          val timestamp = when (date) {
            ActionDate.NOW -> Clock.System.now()
            ActionDate.AIR_DATE -> episode.firstAired ?: Clock.System.now()
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

  suspend fun addEpisodeWatch(episodeId: Long, timestamp: Instant) {
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

    if (needUpdate && traktAuthRepository.state.value == TraktAuthState.LOGGED_IN) {
      fetchEpisodeWatchesFromRemote(episodeId)
    }
  }

  suspend fun updateShowEpisodeWatches(showId: Long) {
    if (traktAuthRepository.state.value != TraktAuthState.LOGGED_IN) return

    val response = traktEpisodeWatchesDataSource.getShowEpisodeWatches(showId)

    val watches = response.mapNotNull { (episode, watchEntry) ->
      val epId = episodesDao.episodeIdWithTraktId(episode.traktId!!)
        ?: return@mapNotNull null // We don't have the episode, skip
      watchEntry.copy(episodeId = epId)
    }
    episodeWatchStore.syncShowWatchEntries(showId, watches)
    episodeWatchLastLastRequestStore.updateLastRequest(showId)
  }

  private suspend fun fetchEpisodeWatchesFromRemote(episodeId: Long) {
    val response = traktEpisodeWatchesDataSource.getEpisodeWatches(episodeId, null)
    val watches = response.map { it.copy(episodeId = episodeId) }
    episodeWatchStore.syncEpisodeWatchEntries(episodeId, watches)
  }

  /**
   * Process any pending episode watch deletes.
   *
   * @return true if a network service was updated
   */
  private suspend fun processPendingDeletes(entries: List<EpisodeWatchEntry>): Boolean {
    if (traktAuthRepository.state.value == TraktAuthState.LOGGED_IN) {
      val localOnlyDeletes = entries.filter { it.traktId == null }
      // If we've got deletes which are local only, just remove them from the DB
      if (localOnlyDeletes.isNotEmpty()) {
        episodeWatchStore.deleteEntriesWithIds(localOnlyDeletes.map(EpisodeWatchEntry::id))
      }

      if (entries.size > localOnlyDeletes.size) {
        val toRemove = entries.filter { it.traktId != null }
        traktEpisodeWatchesDataSource.removeEpisodeWatches(toRemove)
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
    if (traktAuthRepository.state.value == TraktAuthState.LOGGED_IN) {
      traktEpisodeWatchesDataSource.addEpisodeWatches(entries)
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
