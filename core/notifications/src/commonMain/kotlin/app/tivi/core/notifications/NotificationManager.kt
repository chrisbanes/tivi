// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.notifications

import app.tivi.data.models.Notification
import app.tivi.data.models.NotificationChannel
import kotlinx.datetime.Instant

interface NotificationManager {

  suspend fun schedule(
    id: String,
    title: String,
    message: String,
    channel: NotificationChannel,
    date: Instant,
    deeplinkUrl: String? = null,
  )

  suspend fun getPendingNotifications(): List<Notification>
}
