// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.notifications

import app.tivi.data.models.Notification

internal object EmptyNotificationManager : NotificationManager {
  override suspend fun schedule(notification: Notification) {
    // no-op
  }

  override suspend fun getPendingNotifications(): List<Notification> = emptyList()
}
