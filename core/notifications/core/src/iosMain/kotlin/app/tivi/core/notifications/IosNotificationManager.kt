// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.notifications

import app.tivi.data.models.Notification
import app.tivi.data.models.NotificationChannel
import app.tivi.util.Logger
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toKotlinInstant
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

  override suspend fun schedule(notification: Notification) {
    val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
      dateComponents = notification.date
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .toNSDateComponents(),
      repeats = false,
    )

    val userInfo = buildMap {
      if (notification.deeplinkUrl != null) {
        put(USER_INFO_DEEPLINK, notification.deeplinkUrl)
      }
    }

    val content = UNMutableNotificationContent().apply {
      setTitle(notification.title)
      setBody(notification.message)
      setCategoryIdentifier(notification.channel.id)
      @Suppress("UNCHECKED_CAST")
      setUserInfo(userInfo as Map<Any?, *>)
    }

    val request = UNNotificationRequest.requestWithIdentifier(notification.id, content, trigger)

    logger.d {
      buildString {
        append("Scheduling notification. ")
        append(notification)
        append(", trigger:[${trigger.nextTriggerDate()}]")
      }
    }

    suspendCoroutine { cont ->
      UNUserNotificationCenter
        .currentNotificationCenter()
        .addNotificationRequest(request) { error ->
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

  override suspend fun getPendingNotifications(): List<Notification> {
    return suspendCoroutine { cont ->
      UNUserNotificationCenter
        .currentNotificationCenter()
        .getPendingNotificationRequestsWithCompletionHandler { requests ->
          val pending = (requests ?: emptyList<UNNotificationRequest>())
            .asSequence()
            .filterIsInstance<UNNotificationRequest>()
            .map { request ->
              val content = request.content
              Notification(
                id = request.identifier,
                title = content.title,
                message = content.body,
                channel = NotificationChannel.fromId(content.categoryIdentifier),
                date = (request.trigger as? UNCalendarNotificationTrigger)
                  ?.nextTriggerDate()
                  ?.toKotlinInstant()
                  ?: Instant.DISTANT_PAST,
                deeplinkUrl = content.userInfo[USER_INFO_DEEPLINK]?.toString(),
              )
            }
            .toList()

          cont.resume(pending)
        }
    }
  }

  override suspend fun cancelAll(notifications: List<Notification>) {
    val ids = notifications.map { it.id }

    UNUserNotificationCenter
      .currentNotificationCenter()
      .removePendingNotificationRequestsWithIdentifiers(ids)
  }

  override suspend fun cancel(notification: Notification) {
    UNUserNotificationCenter
      .currentNotificationCenter()
      .removePendingNotificationRequestsWithIdentifiers(listOf(notification.id))
  }

  private companion object {
    const val USER_INFO_DEEPLINK = "deeplink_uri"
  }
}
