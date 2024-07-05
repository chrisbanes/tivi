// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.interactors

import app.tivi.core.notifications.NotificationManager
import app.tivi.data.episodes.SeasonsEpisodesRepository
import app.tivi.domain.Interactor
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.Logger
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class ScheduleEpisodeNotifications(
  seasonsEpisodesRepository: Lazy<SeasonsEpisodesRepository>,
  notificationManager: Lazy<NotificationManager>,
  private val logger: Logger,
  private val dispatchers: AppCoroutineDispatchers,
) : Interactor<Unit, Unit>() {
  private val seasonsEpisodesRepository by seasonsEpisodesRepository
  private val notificationManager by notificationManager

  override suspend fun doWork(params: Unit) {
    val episodes = withContext(dispatchers.io) {
      seasonsEpisodesRepository.getUpcomingEpisodesFromFollowedShows()
    }

    if (episodes.isNotEmpty()) {
      episodes.forEach { item ->
        logger.d {
          "Scheduling notification for show episode: " +
            "${item.show.title} - ${item.season.number}x${item.episode.number}. " +
            "Airing: ${item.episode.firstAired}"
        }
      }


      // notificationManager.
    }
  }
}
