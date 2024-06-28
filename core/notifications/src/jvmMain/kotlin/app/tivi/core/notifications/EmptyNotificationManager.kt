// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.notifications

import kotlinx.datetime.LocalDateTime

internal object EmptyNotificationManager: NotificationManager {
  override fun schedule(id: Long, title: String, message: String, date: LocalDateTime) {
    // no-op
  }
}
