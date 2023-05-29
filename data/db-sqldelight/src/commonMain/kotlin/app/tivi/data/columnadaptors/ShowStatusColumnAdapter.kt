// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.columnadaptors

import app.cash.sqldelight.ColumnAdapter
import app.tivi.data.models.ShowStatus
import app.tivi.extensions.unsafeLazy

internal object ShowStatusColumnAdapter : ColumnAdapter<ShowStatus, String> {
    private val values by unsafeLazy { ShowStatus.values().associateBy(ShowStatus::storageKey) }

    override fun decode(databaseValue: String): ShowStatus = values.getValue(databaseValue)

    override fun encode(value: ShowStatus): String = value.storageKey
}
