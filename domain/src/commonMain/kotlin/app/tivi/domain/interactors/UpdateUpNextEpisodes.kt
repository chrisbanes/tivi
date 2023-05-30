// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.interactors

import app.tivi.data.daos.WatchedShowDao
import app.tivi.data.episodes.SeasonsEpisodesRepository
import app.tivi.domain.Interactor
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.parallelForEach
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class UpdateUpNextEpisodes(
    private val watchedShowsDao: WatchedShowDao,
    private val seasonEpisodeRepository: SeasonsEpisodesRepository,
    private val updateLibraryShows: UpdateLibraryShows,
    private val dispatchers: AppCoroutineDispatchers,
) : Interactor<UpdateUpNextEpisodes.Params, Unit>() {

    override suspend fun doWork(params: Params) {
        updateLibraryShows(UpdateLibraryShows.Params(params.forceRefresh))

        // Now update the next episodes, to fetch images, etc
        withContext(dispatchers.io) {
            watchedShowsDao.getUpNextShows().parallelForEach { entry ->
                if (seasonEpisodeRepository.needEpisodeUpdate(entry.episode.id)) {
                    seasonEpisodeRepository.updateEpisode(entry.episode.id)
                }
            }
        }
    }

    data class Params(val forceRefresh: Boolean)
}
