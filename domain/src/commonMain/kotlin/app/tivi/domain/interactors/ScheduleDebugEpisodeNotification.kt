// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.interactors

import app.tivi.app.ApplicationInfo
import app.tivi.core.notifications.NotificationManager
import app.tivi.data.episodes.SeasonsEpisodesRepository
import app.tivi.data.models.NotificationChannel
import app.tivi.domain.Interactor
import app.tivi.settings.TiviPreferences
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.Logger
import app.tivi.util.TiviDateFormatter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import me.tatarka.inject.annotations.Inject

@Inject
class ScheduleDebugEpisodeNotification(
  seasonsEpisodesRepository: Lazy<SeasonsEpisodesRepository>,
  notificationManager: Lazy<NotificationManager>,
  dateTimeFormatter: Lazy<TiviDateFormatter>,
  private val dispatchers: AppCoroutineDispatchers,
  private val applicationInfo: ApplicationInfo,
  private val preferences: Lazy<TiviPreferences>,
  private val logger: Logger,
) : Interactor<ScheduleDebugEpisodeNotification.Params, Unit>() {
  private val seasonsEpisodesRepository by seasonsEpisodesRepository
  private val notificationManager by notificationManager
  private val dateTimeFormatter by dateTimeFormatter

  override suspend fun doWork(params: Params) {
    val notificationsEnabled = withContext(dispatchers.io) {
      preferences.value.episodeAiringNotificationsEnabled.get()
    }

    if (!notificationsEnabled) {
      logger.d {
        "ScheduleDebugEpisodeNotification. " +
          "Notifications not enabled. " +
          "Cancelling all EPISODES_AIRING notifications"
      }
      notificationManager.cancelAllInChannel(NotificationChannel.EPISODES_AIRING)
      return
    }

    val episode = withContext(dispatchers.io) {
      seasonsEpisodesRepository
        .getUpcomingEpisodesFromFollowedShows(Clock.System.now() + 365.days)
        .random()
    }

    val notification = createEpisodeAiringNotification(
      item = episode,
      applicationInfo = applicationInfo,
      dateTimeFormatter = dateTimeFormatter,
    ).copy(date = Clock.System.now() + params.delay)

    notificationManager.schedule(notification)
  }

  data class Params(val delay: Duration)
}
