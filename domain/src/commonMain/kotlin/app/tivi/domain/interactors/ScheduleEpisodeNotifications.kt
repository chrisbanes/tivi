// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.interactors

import app.tivi.core.notifications.NotificationManager
import app.tivi.data.episodes.SeasonsEpisodesRepository
import app.tivi.domain.Interactor
import me.tatarka.inject.annotations.Inject

@Inject
class ScheduleEpisodeNotifications(
  seasonsEpisodesRepository: Lazy<SeasonsEpisodesRepository>,
  notificationManager: Lazy<NotificationManager>,
) : Interactor<Unit, Unit>() {
  private val seasonsEpisodesRepository by seasonsEpisodesRepository
  private val notificationManager by notificationManager

  override suspend fun doWork(params: Unit) {
    val episodes = seasonsEpisodesRepository.getUpcomingEpisodesFromFollowedShows()
    if (episodes.isNotEmpty()) {
      // notificationManager.
    }
  }
}
