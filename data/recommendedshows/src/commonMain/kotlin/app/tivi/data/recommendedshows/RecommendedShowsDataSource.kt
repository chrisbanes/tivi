// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.recommendedshows

import app.tivi.data.models.TiviShow

interface RecommendedShowsDataSource {
    suspend operator fun invoke(page: Int, pageSize: Int): List<TiviShow>
}
