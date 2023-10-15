// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.columnadaptors

import app.cash.sqldelight.ColumnAdapter
import app.tivi.data.models.ShowStatus

internal object ShowStatusColumnAdapter : ColumnAdapter<ShowStatus, String> {
    override fun decode(databaseValue: String): ShowStatus {
        return ShowStatus.entries.first { it.storageKey == databaseValue }
    }

    override fun encode(value: ShowStatus): String = value.storageKey
}
