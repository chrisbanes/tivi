// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.notifications

import kotlinx.datetime.LocalDateTime

interface NotificationManager {

  fun schedule(
    id: String,
    title: String,
    message: String,
    date: LocalDateTime,
    channel: NotificationChannel,
  )

  // fun cancel(title: String)

}


enum class NotificationChannel {
  EPISODES_AIRING,
}
