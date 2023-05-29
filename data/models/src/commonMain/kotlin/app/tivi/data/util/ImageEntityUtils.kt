// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.util

import app.tivi.data.models.ImageType
import app.tivi.data.models.TmdbImageEntity

internal fun <T : TmdbImageEntity> findHighestRatedItem(items: Collection<T>, type: ImageType): T? {
    if (items.size <= 1) {
        return items.firstOrNull()
    }
    return items.asSequence()
        .filter { it.type == type }
        .maxByOrNull { it.rating + (if (it.isPrimary) 10f else 0f) }
}
