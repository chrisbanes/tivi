// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.notifications

import app.tivi.data.models.Notification

interface NotificationManager {

  suspend fun schedule(notification: Notification) = Unit

  suspend fun cancel(notification: Notification) = Unit

  suspend fun getPendingNotifications(): List<Notification> = emptyList()
}
