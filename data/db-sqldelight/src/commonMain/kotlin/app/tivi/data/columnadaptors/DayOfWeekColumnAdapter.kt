// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.columnadaptors

import app.cash.sqldelight.ColumnAdapter
import app.tivi.extensions.unsafeLazy
import kotlinx.datetime.DayOfWeek

internal object DayOfWeekColumnAdapter : ColumnAdapter<DayOfWeek, Long> {
    private val values by unsafeLazy { DayOfWeek.values().associateBy { it.dbValue } }

    override fun decode(databaseValue: Long): DayOfWeek = values.getValue(databaseValue.toInt())

    override fun encode(value: DayOfWeek): Long = value.dbValue.toLong()
}

/**
 * These follows the ISO-8601 standard, from 1 (Monday) to 7 (Sunday)
 */
internal val DayOfWeek.dbValue: Int
    get() = when (this) {
        DayOfWeek.MONDAY -> 1
        DayOfWeek.TUESDAY -> 2
        DayOfWeek.WEDNESDAY -> 3
        DayOfWeek.THURSDAY -> 4
        DayOfWeek.FRIDAY -> 5
        DayOfWeek.SATURDAY -> 6
        DayOfWeek.SUNDAY -> 7
        else -> error("Unknown DayOfWeek: $this")
    }
