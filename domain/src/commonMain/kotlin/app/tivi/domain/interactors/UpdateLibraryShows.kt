// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.interactors

import app.tivi.data.daos.WatchedShowDao
import app.tivi.data.episodes.SeasonsEpisodesRepository
import app.tivi.data.followedshows.FollowedShowsRepository
import app.tivi.data.models.FollowedShowEntry
import app.tivi.data.models.WatchedShowEntry
import app.tivi.data.shows.ShowStore
import app.tivi.data.util.dataSetChanged
import app.tivi.data.util.fetch
import app.tivi.data.watchedshows.WatchedShowsLastRequestStore
import app.tivi.data.watchedshows.WatchedShowsStore
import app.tivi.domain.Interactor
import app.tivi.domain.UserInitiatedParams
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.Logger
import app.tivi.util.parallelForEach
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class UpdateLibraryShows(
  private val followedShowsRepository: FollowedShowsRepository,
  private val seasonEpisodeRepository: SeasonsEpisodesRepository,
  private val showStore: ShowStore,
  private val watchedShowsLastRequestStore: WatchedShowsLastRequestStore,
  private val watchedShowsStore: WatchedShowsStore,
  private val watchedShowDao: WatchedShowDao,
  private val logger: Logger,
  private val dispatchers: AppCoroutineDispatchers,
  scheduleEpisodeNotifications: Lazy<ScheduleEpisodeNotifications>,
) : Interactor<UpdateLibraryShows.Params, Unit>() {
  private val scheduleEpisodeNotifications by scheduleEpisodeNotifications

  override suspend fun doWork(params: Params): Unit = withContext(dispatchers.io) {
    val watchedShowsDeferred = async {
      // We use a low threshold here as the data contains a 'last updated' value.
      // It's a quick way to know whether to cascade the updates below
      watchedShowsStore.fetch(
        key = Unit,
        forceFresh = params.isUserInitiated || watchedShowsLastRequestStore.isRequestExpired(1.hours),
      )
    }
    val followedShowsDeferred = async {
      if (params.isUserInitiated || followedShowsRepository.needFollowedShowsSync()) {
        val result = followedShowsRepository.syncFollowedShows()
        if (result.dataSetChanged()) {
          scheduleEpisodeNotifications(ScheduleEpisodeNotifications.Params())
        }
      }
      followedShowsRepository.getFollowedShows()
    }

    // await the watched shows and followed shows update. We need both
    val libraryShowIds = buildSet {
      addAll(followedShowsDeferred.await().map(FollowedShowEntry::showId))
      addAll(watchedShowsDeferred.await().map(WatchedShowEntry::showId))
    }

    // Finally sync the seasons/episodes and watches
    libraryShowIds.parallelForEach { entryShowId ->
      ensureActive()
      showStore.fetch(entryShowId)

      ensureActive()
      try {
        with(seasonEpisodeRepository) {
          val watchedEntry = watchedShowDao.entryWithShowId(entryShowId)
          if (needShowSeasonsUpdate(entryShowId, watchedEntry?.lastUpdated)) {
            updateSeasonsEpisodes(entryShowId)
          }

          ensureActive()

          if (needShowEpisodeWatchesSync(entryShowId, watchedEntry?.lastUpdated)) {
            updateShowEpisodeWatches(entryShowId)
          }
        }
      } catch (ce: CancellationException) {
        throw ce
      } catch (t: Throwable) {
        logger.e(t) { "Error while updating show seasons/episodes: $entryShowId" }
      }
    }
  }

  data class Params(override val isUserInitiated: Boolean) : UserInitiatedParams
}
