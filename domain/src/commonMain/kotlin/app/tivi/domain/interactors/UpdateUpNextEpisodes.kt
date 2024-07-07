// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.interactors

import app.tivi.data.daos.WatchedShowDao
import app.tivi.data.episodes.SeasonsEpisodesRepository
import app.tivi.domain.Interactor
import app.tivi.domain.UserInitiatedParams
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.parallelForEach
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class UpdateUpNextEpisodes(
  private val watchedShowsDao: Lazy<WatchedShowDao>,
  private val seasonEpisodeRepository: Lazy<SeasonsEpisodesRepository>,
  private val updateLibraryShows: Lazy<UpdateLibraryShows>,
  private val dispatchers: AppCoroutineDispatchers,
) : Interactor<UpdateUpNextEpisodes.Params, Unit>() {

  override suspend fun doWork(params: Params) {
    updateLibraryShows.value.invoke(UpdateLibraryShows.Params(params.isUserInitiated))

    // Now update the next episodes, to fetch images, etc
    withContext(dispatchers.io) {
      watchedShowsDao.value.getUpNextShows().parallelForEach { entry ->
        if (seasonEpisodeRepository.value.needEpisodeUpdate(entry.episode.id)) {
          seasonEpisodeRepository.value.updateEpisode(entry.episode.id)
        }
      }
    }
  }

  data class Params(override val isUserInitiated: Boolean) : UserInitiatedParams
}
