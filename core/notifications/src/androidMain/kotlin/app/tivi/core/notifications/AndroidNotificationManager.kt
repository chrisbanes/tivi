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
import androidx.datastore.dataStoreFile
import app.tivi.common.ui.resources.EnTiviStrings
import app.tivi.core.notifications.proto.PendingNotification as PendingNotificationsProto
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.Logger
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import me.tatarka.inject.annotations.Inject
import okio.Path.Companion.toOkioPath

@Inject
class AndroidNotificationManager(
  private val application: Application,
  private val logger: Logger,
  private val dispatchers: AppCoroutineDispatchers,
) : NotificationManager {

  private val coroutineScope by lazy {
    CoroutineScope(dispatchers.io + SupervisorJob())
  }

  private val store by lazy {
    pendingNotificationsStore(coroutineScope) {
      application.dataStoreFile("pending_notifications.pb")
        .absoluteFile
        .toOkioPath()
    }
  }

  private val notificationManager by lazy { NotificationManagerCompat.from(application) }
  private val alarmManager by lazy { application.getSystemService<AlarmManager>()!! }

  // TODO: this should use the system strings
  private val strings = EnTiviStrings

  override suspend fun schedule(
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
      channelId = channel.id,
      title = title,
      text = message,
    )

    val windowStartTime = (date - ALARM_WINDOW_LENGTH)

    if (windowStartTime <= Clock.System.now()) {
      // If the window start time is in the past, just send it now
      logger.d {
        buildString {
          append("Sending notification now. ")
          append("title:[$title], ")
          append("message:[$message], ")
          append("id:[$id], ")
          append("channel:[$channel]")
        }
      }
      application.sendBroadcast(intent)
    } else {
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

      store.updateData { data ->
        val new = PendingNotificationsProto(id = id, date = date.toWireInstant())

        data.copy(
          pending = (data.pending + new).distinctBy { it.id },
        )
      }
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

  override suspend fun getPendingNotifications(): List<PendingNotification> {
    return store.data.firstOrNull()?.let { data ->
      data.pending.map {
        PendingNotification(it.id, it.date?.toKotlinInstant())
      }
    } ?: emptyList()
  }

  private companion object {
    // We request 10 mins as Android S can choose to apply a minimum of 10 mins anyway
    // Being earlier is better than being late
    val ALARM_WINDOW_LENGTH = 10.minutes

    const val PENDING_INTENT_FLAGS = FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT or FLAG_ONE_SHOT
  }
}
