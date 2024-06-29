// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.notifications

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

  override suspend fun schedule(
    id: String,
    title: String,
    message: String,
    channel: NotificationChannel,
    date: Instant,
    deeplinkUrl: String?,
  ) {
    val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
      dateComponents = date.toLocalDateTime(TimeZone.currentSystemDefault()).toNSDateComponents(),
      repeats = false,
    )

    val userInfo = buildMap {
      if (deeplinkUrl != null) {
        put(USER_INFO_DEEPLINK, deeplinkUrl)
      }
    }

    val content = UNMutableNotificationContent().apply {
      setTitle(title)
      setBody(message)
      setCategoryIdentifier(channel.id)
      setUserInfo(userInfo as Map<Any?, *>)
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

  override suspend fun getPendingNotifications(): List<PendingNotification> {
    return suspendCoroutine { cont ->
      UNUserNotificationCenter
        .currentNotificationCenter()
        .getPendingNotificationRequestsWithCompletionHandler { requests ->
          val pending = (requests ?: emptyList<UNNotificationRequest>())
            .asSequence()
            .filterIsInstance<UNNotificationRequest>()
            .map { request ->
              val content = request.content
              PendingNotification(
                id = request.identifier,
                title = content.title,
                message = content.body,
                channel = notificationChannelFromId(content.categoryIdentifier),
                date = (request.trigger as? UNCalendarNotificationTrigger)
                  ?.nextTriggerDate()
                  ?.toKotlinInstant(),
                deeplinkUrl = content.userInfo[USER_INFO_DEEPLINK]?.toString(),
              )
            }
            .toList()

          cont.resume(pending)
        }
    }
  }

  private companion object {
    const val USER_INFO_DEEPLINK = "deeplink_url"
  }
}
