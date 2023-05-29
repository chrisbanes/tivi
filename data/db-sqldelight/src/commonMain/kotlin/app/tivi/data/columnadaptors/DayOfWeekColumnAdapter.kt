// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.columnadaptors

import app.cash.sqldelight.ColumnAdapter
import app.tivi.extensions.unsafeLazy
import kotlinx.datetime.DayOfWeek

internal object DayOfWeekColumnAdapter : ColumnAdapter<DayOfWeek, Long> {
    private val values by unsafeLazy { DayOfWeek.values().associateBy { it.value } }

    override fun decode(databaseValue: Long): DayOfWeek = values.getValue(databaseValue.toInt())

    override fun encode(value: DayOfWeek): Long = value.value.toLong()
}
