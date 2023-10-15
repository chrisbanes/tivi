// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.columnadaptors

import app.cash.sqldelight.ColumnAdapter
import app.tivi.data.models.ImageType

internal object ImageTypeColumnAdapter : ColumnAdapter<ImageType, String> {
    override fun decode(databaseValue: String): ImageType {
        return ImageType.entries.first { it.storageKey == databaseValue }
    }

    override fun encode(value: ImageType): String = value.storageKey
}
