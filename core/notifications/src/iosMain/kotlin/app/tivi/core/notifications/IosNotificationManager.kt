// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.notifications

import app.tivi.util.Logger
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toNSDateComponents
import me.tatarka.inject.annotations.Inject
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter

@Inject
class IosNotificationManager(
  private val logger: Logger,
) : NotificationManager {

  private val notificationCenter: UNUserNotificationCenter by lazy {
    UNUserNotificationCenter.currentNotificationCenter()
  }

  override suspend fun schedule(
    id: String,
    title: String,
    message: String,
    channel: NotificationChannel,
    date: Instant,
  ) {
    val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
      dateComponents = date.toLocalDateTime(TimeZone.currentSystemDefault()).toNSDateComponents(),
      repeats = false,
    )

    val content = UNMutableNotificationContent().apply {
      setTitle(title)
      setBody(message)
      setCategoryIdentifier(channel.id)
    }

    val request = UNNotificationRequest.requestWithIdentifier(id, content, trigger)

    logger.d {
      buildString {
        append("Scheduling notification. ")
        append("title:[$title], ")
        append("message:[$message], ")
        append("id:[$id], ")
        append("channel:[$channel], ")
        append("trigger:[${trigger.nextTriggerDate()}]")
      }
    }

    suspendCoroutine { cont ->
      notificationCenter.addNotificationRequest(request) { error ->
        when (error) {
          null -> cont.resume(Unit)
          else -> {
            cont.resumeWithException(
              IllegalArgumentException("Error occurred during addNotificationRequest: $error"),
            )
          }
        }
      }
    }
  }

  override suspend fun getPendingNotifications(): List<PendingNotification> {
    return suspendCoroutine { cont ->
      notificationCenter.getPendingNotificationRequestsWithCompletionHandler { requests ->
        val pending = (requests ?: emptyList<UNNotificationRequest>())
          .asSequence()
          .filterIsInstance<UNNotificationRequest>()
          .map { request ->
            PendingNotification(
              id = request.identifier,
              title = request.content.title,
              message = request.content.body,
              channel = notificationChannelFromId(request.content.categoryIdentifier),
            )
          }
          .toList()

        cont.resume(pending)
      }
    }
  }
}
