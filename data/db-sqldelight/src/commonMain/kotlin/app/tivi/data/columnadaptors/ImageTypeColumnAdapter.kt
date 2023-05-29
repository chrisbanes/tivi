// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.columnadaptors

import app.cash.sqldelight.ColumnAdapter
import app.tivi.data.models.ImageType
import app.tivi.extensions.unsafeLazy

internal object ImageTypeColumnAdapter : ColumnAdapter<ImageType, String> {
    private val values by unsafeLazy { ImageType.values().associateBy(ImageType::storageKey) }

    override fun decode(databaseValue: String): ImageType = values.getValue(databaseValue)

    override fun encode(value: ImageType): String = value.storageKey
}
