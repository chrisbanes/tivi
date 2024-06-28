// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.notifications

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toNSDateComponents
import platform.Foundation.NSError
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter

internal class IosNotificationManager : NotificationManager {

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
      dateComponents = date.toLocalDateTime(TimeZone.currentSystemDefault())
        .toNSDateComponents(),
      repeats = false,
    )

    request(
      id = id,
      title = title,
      message = message,
      trigger = trigger
    )
  }

  override fun notify(id: String, title: String, message: String, channel: NotificationChannel) {
    request(id = id, title = title, message = message)
  }

  private fun request(
    id: String,
    title: String,
    message: String,
    trigger: UNNotificationTrigger? = null,
  ) {
    val content = UNMutableNotificationContent().apply {
      setTitle(title)
      setBody(message)
    }

    val request = UNNotificationRequest.requestWithIdentifier(id, content, trigger)

    val withCompletionHandler: (NSError?) -> Unit = { error: NSError? ->
      println("Notification completed with: $error")
    }

    notificationCenter.addNotificationRequest(request, withCompletionHandler)
  }
}
