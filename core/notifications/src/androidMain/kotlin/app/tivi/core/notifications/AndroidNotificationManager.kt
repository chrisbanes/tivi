// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.notifications

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_DEFAULT
import androidx.core.content.getSystemService
import app.tivi.common.ui.resources.EnTiviStrings
import app.tivi.util.Logger
import kotlin.time.Duration.Companion.minutes
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject

@Inject
class AndroidNotificationManager(
  private val application: Application,
  private val logger: Logger,
) : NotificationManager {

  private val notificationManager by lazy { NotificationManagerCompat.from(application) }
  private val alarmManager by lazy { application.getSystemService<AlarmManager>()!! }

  // TODO: this should use the system strings
  private val strings = EnTiviStrings

  override fun schedule(
    id: String,
    title: String,
    message: String,
    channel: NotificationChannel,
    date: Instant,
  ) {
    // We create the channel now ahead of time. We want to limit the amount of work
    // in the broadcast receiver
    notificationManager.createChannel(channel)

    val intent = PostNotificationBroadcastReceiver.buildIntent(
      context = application,
      id = id,
      channelId = channel.toChannelId(),
      title = title,
      text = message,
    )

    val windowStartTime = (date - ALARM_WINDOW_LENGTH).coerceAtLeast(Clock.System.now())

    logger.d {
      buildString {
        append("Scheduling notification. ")
        append("title:[$title], ")
        append("message:[$message], ")
        append("id:[$id], ")
        append("channel:[$channel], ")
        append("windowStartTime:[$windowStartTime], ")
        append("window:[$ALARM_WINDOW_LENGTH]")
      }
    }

    alarmManager.setWindow(
      // type
      AlarmManager.RTC_WAKEUP,
      // windowStartMillis. We minus our defined window from the provide date/time
      windowStartTime.toEpochMilliseconds(),
      // windowLengthMillis
      ALARM_WINDOW_LENGTH.inWholeMilliseconds,
      // operation
      PendingIntent.getBroadcast(application, id.hashCode(), intent, PENDING_INTENT_FLAGS),
    )
  }

  override fun notify(id: String, title: String, message: String, channel: NotificationChannel) {
    notificationManager.createChannel(channel)

    val intent = PostNotificationBroadcastReceiver.buildIntent(
      context = application,
      id = id,
      channelId = channel.toChannelId(),
      title = title,
      text = message,
    )

    application.sendBroadcast(intent)
  }

  private fun NotificationManagerCompat.createChannel(channel: NotificationChannel) {
    val androidChannel =
      NotificationChannelCompat.Builder(channel.id, IMPORTANCE_DEFAULT)
        .apply {
          when (channel) {
            NotificationChannel.EPISODES_AIRING -> {
              setName(strings.settingsNotificationsAiringEpisodesTitle)
              setDescription(strings.settingsNotificationsAiringEpisodesSummary)
              setVibrationEnabled(true)
            }

            NotificationChannel.DEVELOPER -> {
              setName("Developer testing")
              setVibrationEnabled(true)
            }
          }
        }
        .build()

    createNotificationChannel(androidChannel)
  }

  private companion object {
    // We request 10 mins as Android S can choose to apply a minimum of 10 mins anyway
    // Being earlier is better than being late
    val ALARM_WINDOW_LENGTH = 10.minutes

    const val PENDING_INTENT_FLAGS = FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT or FLAG_ONE_SHOT
  }
}
