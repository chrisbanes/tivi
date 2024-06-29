// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.notifications

import app.tivi.util.Logger
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toNSDateComponents
import me.tatarka.inject.annotations.Inject
import platform.Foundation.NSError
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

  override fun schedule(
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

    val withCompletionHandler: (NSError?) -> Unit = { error: NSError? ->
      logger.d { "Notification completed. Error:[$error]" }
    }

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

    notificationCenter.addNotificationRequest(request, withCompletionHandler)
  }
}
