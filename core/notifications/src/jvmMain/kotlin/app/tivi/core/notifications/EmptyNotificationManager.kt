// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.notifications

import kotlinx.datetime.Instant

internal object EmptyNotificationManager : NotificationManager {
  override suspend fun schedule(
    id: String,
    title: String,
    message: String,
    channel: NotificationChannel,
    date: Instant,
  ) {
    // no-op
  }

  override suspend fun getPendingNotifications(): List<PendingNotification> = emptyList()
}
