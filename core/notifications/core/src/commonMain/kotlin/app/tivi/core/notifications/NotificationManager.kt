// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.notifications

import app.tivi.data.models.Notification
import app.tivi.data.models.NotificationChannel

interface NotificationManager {

  suspend fun schedule(notification: Notification) = Unit

  suspend fun cancel(notification: Notification) = Unit

  suspend fun cancelAll(notifications: List<Notification>) {
    notifications.forEach { cancel(it) }
  }

  suspend fun cancelAllInChannel(channel: NotificationChannel) {
    cancelAll(
      getPendingNotifications().filter { it.channel == channel },
    )
  }

  suspend fun getPendingNotifications(): List<Notification> = emptyList()
}
