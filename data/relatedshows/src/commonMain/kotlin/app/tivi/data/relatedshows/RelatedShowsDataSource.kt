// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.relatedshows

import app.tivi.data.models.RelatedShowEntry
import app.tivi.data.models.TiviShow

fun interface RelatedShowsDataSource {
    suspend operator fun invoke(
        showId: Long,
    ): List<Pair<TiviShow, RelatedShowEntry>>
}
