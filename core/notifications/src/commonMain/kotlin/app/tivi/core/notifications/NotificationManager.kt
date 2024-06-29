// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.notifications

import kotlinx.datetime.Instant

interface NotificationManager {

  suspend fun schedule(
    id: String,
    title: String,
    message: String,
    channel: NotificationChannel,
    date: Instant,
  )

  suspend fun getPendingNotifications(): List<PendingNotification>
}

data class PendingNotification(val id: String, val date: Instant?)

enum class NotificationChannel(val id: String) {
  DEVELOPER("dev"),
  EPISODES_AIRING("episodes_airing"),
}
