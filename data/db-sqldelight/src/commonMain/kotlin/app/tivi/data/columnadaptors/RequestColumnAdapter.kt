// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.columnadaptors

import app.cash.sqldelight.ColumnAdapter
import app.tivi.data.models.Request
import app.tivi.extensions.unsafeLazy

internal object RequestColumnAdapter : ColumnAdapter<Request, String> {
    private val values by unsafeLazy { Request.values().associateBy(Request::tag) }

    override fun decode(databaseValue: String): Request = values.getValue(databaseValue)
    override fun encode(value: Request): String = value.tag
}
