// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.columnadaptors

import app.cash.sqldelight.ColumnAdapter
import app.tivi.data.models.PendingAction
import app.tivi.extensions.unsafeLazy

internal object PendingActionColumnAdapter : ColumnAdapter<PendingAction, String> {
    private val values by unsafeLazy { PendingAction.values().associateBy(PendingAction::value) }

    override fun decode(databaseValue: String): PendingAction = values.getValue(databaseValue)

    override fun encode(value: PendingAction): String = value.value
}
