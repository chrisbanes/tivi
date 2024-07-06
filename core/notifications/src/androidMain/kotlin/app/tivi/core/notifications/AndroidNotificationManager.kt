// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.notifications

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_DEFAULT
import androidx.core.content.getSystemService
import app.tivi.common.ui.resources.EnTiviStrings
import app.tivi.data.models.Notification
import app.tivi.data.models.NotificationChannel
import app.tivi.util.Logger
import kotlin.time.Duration.Companion.minutes
import kotlinx.datetime.Clock
import me.tatarka.inject.annotations.Inject

@Inject
class AndroidNotificationManager(
  private val application: Application,
  private val logger: Logger,
  private val store: PendingNotificationStore,
) : NotificationManager {
  private val notificationManager by lazy { NotificationManagerCompat.from(application) }
  private val alarmManager by lazy { application.getSystemService<AlarmManager>()!! }

  // TODO: this should use the system strings
  private val strings = EnTiviStrings

  override suspend fun schedule(notification: Notification) {
    // We create the channel now ahead of time. We want to limit the amount of work
    // in the broadcast receiver
    notificationManager.createChannel(notification.channel)

    // Save the pending notification
    store.add(notification.toPendingNotification())

    // Now decide whether to send the broadcast now, or set an alarm
    val windowStartTime = (notification.date - ALARM_WINDOW_LENGTH)
    if (windowStartTime <= Clock.System.now()) {
      // If the window start time is in the past, just send it now
      logger.d { "Sending notification now: $notification" }
      application.sendBroadcast(
        PostNotificationBroadcastReceiver.buildIntent(application, notification.id),
      )
    } else {
      logger.d { "Scheduling notification for $windowStartTime: $notification" }

      alarmManager.setWindow(
        // type
        AlarmManager.RTC_WAKEUP,
        // windowStartMillis. We minus our defined window from the provide date/time
        windowStartTime.toEpochMilliseconds(),
        // windowLengthMillis
        ALARM_WINDOW_LENGTH.inWholeMilliseconds,
        // operation
        notification.buildPendingIntent(application),
      )
    }
  }

  private fun NotificationManagerCompat.createChannel(channel: NotificationChannel) {
    val androidChannel = NotificationChannelCompat.Builder(channel.id, IMPORTANCE_DEFAULT)
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

  override suspend fun cancel(notification: Notification) {
    alarmManager.cancel(notification.buildPendingIntent(application))
    store.removeWithId(notification.id)
  }

  override suspend fun getPendingNotifications(): List<Notification> {
    return store.getPendingNotifications()
  }

  private companion object {
    // We request 10 mins as Android S can choose to apply a minimum of 10 mins anyway
    // Being earlier is better than being late
    val ALARM_WINDOW_LENGTH = 10.minutes
  }
}

private const val PENDING_INTENT_FLAGS = FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT or FLAG_ONE_SHOT

internal fun Notification.buildPendingIntent(
  context: Context,
): PendingIntent {
  val intent = PostNotificationBroadcastReceiver.buildIntent(context, id)
  return PendingIntent.getBroadcast(context, id.hashCode(), intent, PENDING_INTENT_FLAGS)
}
