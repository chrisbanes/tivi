// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.notifications

import kotlinx.datetime.Instant

interface NotificationManager {

  fun schedule(
    id: String,
    title: String,
    message: String,
    channel: NotificationChannel,
    date: Instant,
  )

  fun notify(
    id: String,
    title: String,
    message: String,
    channel: NotificationChannel,
  )

  // fun cancel(title: String)

}


enum class NotificationChannel {
  DEVELOPER,
  EPISODES_AIRING,
}
