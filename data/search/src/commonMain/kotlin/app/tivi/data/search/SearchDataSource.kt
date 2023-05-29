// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.search

import app.tivi.data.models.ShowTmdbImage
import app.tivi.data.models.TiviShow

interface SearchDataSource {
    suspend fun search(query: String): List<Pair<TiviShow, List<ShowTmdbImage>>>
}
