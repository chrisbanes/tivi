// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.columnadaptors

import app.cash.sqldelight.ColumnAdapter
import kotlinx.datetime.LocalTime

internal object LocalTimeColumnAdapter : ColumnAdapter<LocalTime, String> {
  override fun decode(databaseValue: String): LocalTime = LocalTime.parse(databaseValue)
  override fun encode(value: LocalTime): String = value.toString()
}
