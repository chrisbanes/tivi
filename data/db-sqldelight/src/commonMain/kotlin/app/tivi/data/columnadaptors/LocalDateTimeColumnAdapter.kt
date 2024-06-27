// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.columnadaptors

import app.cash.sqldelight.ColumnAdapter
import kotlinx.datetime.LocalDateTime

internal object LocalDateTimeColumnAdapter : ColumnAdapter<LocalDateTime, String> {
  override fun decode(databaseValue: String): LocalDateTime = LocalDateTime.parse(databaseValue)
  override fun encode(value: LocalDateTime): String = value.toString()
}
