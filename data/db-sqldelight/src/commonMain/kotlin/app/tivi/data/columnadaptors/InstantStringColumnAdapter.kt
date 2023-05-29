// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.columnadaptors

import app.cash.sqldelight.ColumnAdapter
import kotlinx.datetime.Instant
import kotlinx.datetime.toInstant

internal object InstantStringColumnAdapter : ColumnAdapter<Instant, String> {
    override fun decode(databaseValue: String): Instant = databaseValue.toInstant()
    override fun encode(value: Instant): String = value.toString()
}
