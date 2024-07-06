// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.models

import kotlinx.datetime.Instant

data class Notification(
  val id: String,
  val title: String,
  val message: String,
  val channel: NotificationChannel,
  val date: Instant,
  val deeplinkUrl: String? = null,
)

enum class NotificationChannel(val id: String) {
  DEVELOPER("dev"),
  EPISODES_AIRING("episodes_airing"),
  ;

  companion object {
    fun fromId(id: String): NotificationChannel {
      return NotificationChannel.entries.first { it.id == id }
    }
  }
}
