// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.interactors

import app.tivi.core.notifications.NotificationManager
import app.tivi.data.compoundmodels.ShowSeasonEpisode
import app.tivi.data.episodes.SeasonsEpisodesRepository
import app.tivi.data.models.Notification
import app.tivi.data.models.NotificationChannel
import app.tivi.domain.Interactor
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.Logger
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.tatarka.inject.annotations.Inject

@Inject
class ScheduleEpisodeNotifications(
  seasonsEpisodesRepository: Lazy<SeasonsEpisodesRepository>,
  notificationManager: Lazy<NotificationManager>,
  private val logger: Logger,
  private val dispatchers: AppCoroutineDispatchers,
) : Interactor<ScheduleEpisodeNotifications.Params, Unit>() {
  private val seasonsEpisodesRepository by seasonsEpisodesRepository
  private val notificationManager by notificationManager

  override suspend fun doWork(params: Params) {
    // We can do better here, but this is fine for now
    notificationManager.getPendingNotifications()
      .asSequence()
      .filter { it.channel == NotificationChannel.EPISODES_AIRING }
      .forEach { notificationManager.cancel(it) }

    val episodes = withContext(dispatchers.io) {
      seasonsEpisodesRepository.getUpcomingEpisodesFromFollowedShows(
        Clock.System.now() + params.limit,
      )
    }

    episodes
      .map { createNotification(it, params.bufferTime) }
      .forEach { notificationManager.schedule(it) }
  }

  private fun createNotification(item: ShowSeasonEpisode, bufferTime: Duration): Notification {
    val airDate = requireNotNull(item.episode.firstAired)
    val airDateLocal = airDate.toLocalDateTime(TimeZone.currentSystemDefault())

    return Notification(
      id = "episode_${item.episode.id}",
      title = "Episode airing at ${airDateLocal.time}",
      message = buildString {
        append(item.show.title)
        append(" - ")
        append(item.episode.title)
        append(" [")
        append(item.season.number)
        append('x')
        append(item.episode.number)
        append(']')
      },
      channel = NotificationChannel.EPISODES_AIRING,
      date = requireNotNull(item.episode.firstAired) - bufferTime,
      deeplinkUrl = "app.tivi//foo/${item.episode.id}",
    )
  }

  data class Params(val limit: Duration, val bufferTime: Duration = 10.minutes)
}
