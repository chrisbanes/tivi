// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.notifications

import app.tivi.data.models.Notification
import app.tivi.data.models.NotificationChannel
import kotlinx.datetime.Instant

internal object EmptyNotificationManager : NotificationManager {
  override suspend fun schedule(
    id: String,
    title: String,
    message: String,
    channel: NotificationChannel,
    date: Instant,
    deeplinkUrl: String?,
  ) {
    // no-op
  }

  override suspend fun getPendingNotifications(): List<Notification> = emptyList()
}
