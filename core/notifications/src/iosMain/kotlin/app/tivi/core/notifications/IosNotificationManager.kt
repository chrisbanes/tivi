// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.notifications

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toNSDateComponents
import platform.Foundation.NSError
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter

internal class IosNotificationManager : NotificationManager {

  private val notificationCenter: UNUserNotificationCenter by lazy {
    UNUserNotificationCenter.currentNotificationCenter()
  }

  override fun schedule(
    id: String,
    title: String,
    message: String,
    date: LocalDateTime,
    channel: NotificationChannel,
  ) {
    val content = UNMutableNotificationContent().apply {
      setTitle(title)
      setBody(message)
    }

    val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
      dateComponents = date.toNSDateComponents(),
      repeats = false,
    )

    val request = UNNotificationRequest.requestWithIdentifier(id, content, trigger)

    val withCompletionHandler: (NSError?) -> Unit = { error: NSError? ->
      println("Notification completed with: $error")
    }

    notificationCenter.addNotificationRequest(request, withCompletionHandler)
  }
}
