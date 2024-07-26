// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.interactors

import app.tivi.app.ApplicationInfo
import app.tivi.common.ui.resources.EnTiviStrings
import app.tivi.core.notifications.NotificationManager
import app.tivi.data.compoundmodels.ShowSeasonEpisode
import app.tivi.data.episodes.SeasonsEpisodesRepository
import app.tivi.data.models.Notification
import app.tivi.data.models.NotificationChannel
import app.tivi.domain.Interactor
import app.tivi.entitlements.EntitlementManager
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.TiviDateFormatter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.tatarka.inject.annotations.Inject

@Inject
class ScheduleEpisodeNotifications(
  seasonsEpisodesRepository: Lazy<SeasonsEpisodesRepository>,
  notificationManager: Lazy<NotificationManager>,
  dateTimeFormatter: Lazy<TiviDateFormatter>,
  entitlementManager: Lazy<EntitlementManager>,
  private val dispatchers: AppCoroutineDispatchers,
  private val applicationInfo: ApplicationInfo,
) : Interactor<ScheduleEpisodeNotifications.Params, Unit>() {
  private val seasonsEpisodesRepository by seasonsEpisodesRepository
  private val notificationManager by notificationManager
  private val dateTimeFormatter by dateTimeFormatter
  private val entitlementManager by entitlementManager

  override suspend fun doWork(params: Params) {
    // TODO: we should do something better here
    if (!entitlementManager.hasProEntitlement()) return

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

    episodes.asSequence()
      .map { item ->
        createEpisodeAiringNotification(
          item = item,
          applicationInfo = applicationInfo,
          dateTimeFormatter = dateTimeFormatter,
          bufferTime = params.bufferTime,
        )
      }
      .forEach { notificationManager.schedule(it) }
  }

  data class Params(val limit: Duration = 12.hours, val bufferTime: Duration = 10.minutes)
}

internal fun createEpisodeAiringNotification(
  item: ShowSeasonEpisode,
  applicationInfo: ApplicationInfo,
  dateTimeFormatter: TiviDateFormatter,
  bufferTime: Duration = 0.seconds,
): Notification {
  val airDate = requireNotNull(item.episode.firstAired)
  val airDateLocal = airDate.toLocalDateTime(TimeZone.currentSystemDefault())

  return Notification(
    id = "episode_${item.episode.id}",
    title = EnTiviStrings.notificationEpisodeAiringTitle(item.show.title!!),
    message = EnTiviStrings.notificationEpisodeAiringMessage(
      item.episode.title!!,
      dateTimeFormatter.formatShortTime(airDateLocal.time),
      item.show.network ?: "?",
      item.season.number ?: 99,
      item.episode.number ?: 99,
    ),
    channel = NotificationChannel.EPISODES_AIRING,
    date = requireNotNull(item.episode.firstAired) - bufferTime,
    deeplinkUrl = "${applicationInfo.packageName}://tivi/show/${item.show.id}/season/${item.season.id}/episode/${item.episode.id}",
  )
}
