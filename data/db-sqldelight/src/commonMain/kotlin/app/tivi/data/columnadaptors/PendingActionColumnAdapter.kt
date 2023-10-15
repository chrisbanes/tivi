// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.columnadaptors

import app.cash.sqldelight.ColumnAdapter
import app.tivi.data.models.PendingAction

internal object PendingActionColumnAdapter : ColumnAdapter<PendingAction, String> {
    override fun decode(databaseValue: String): PendingAction {
        return PendingAction.entries.first { it.value == databaseValue }
    }

    override fun encode(value: PendingAction): String = value.value
}
