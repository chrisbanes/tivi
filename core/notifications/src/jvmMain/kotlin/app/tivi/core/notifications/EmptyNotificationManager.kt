// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.notifications

import kotlinx.datetime.Instant

internal object EmptyNotificationManager : NotificationManager {
  override fun schedule(
    id: String,
    title: String,
    message: String,
    channel: NotificationChannel,
    date: Instant,
  ) {
    // no-op
  }

  override fun notify(id: String, title: String, message: String, channel: NotificationChannel) {
    // no-op
  }
}
