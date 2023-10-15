// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.columnadaptors

import app.cash.sqldelight.ColumnAdapter
import app.tivi.data.models.Request

internal object RequestColumnAdapter : ColumnAdapter<Request, String> {
    override fun decode(databaseValue: String): Request {
        return Request.entries.first { it.tag == databaseValue }
    }

    override fun encode(value: Request): String = value.tag
}
