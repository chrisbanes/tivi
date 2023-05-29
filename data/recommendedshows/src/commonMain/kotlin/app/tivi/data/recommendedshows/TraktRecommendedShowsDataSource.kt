// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.recommendedshows

import app.moviebase.trakt.api.TraktRecommendationsApi
import app.tivi.data.mappers.TraktShowToTiviShow
import app.tivi.data.mappers.map
import app.tivi.data.models.TiviShow
import me.tatarka.inject.annotations.Inject

@Inject
class TraktRecommendedShowsDataSource(
    private val recommendationsService: Lazy<TraktRecommendationsApi>,
    private val showMapper: TraktShowToTiviShow,
) : RecommendedShowsDataSource {

    override suspend operator fun invoke(
        page: Int,
        pageSize: Int,
    ): List<TiviShow> =
        recommendationsService.value
            // We add 1 because Trakt uses a 1-based index whereas we use a 0-based index
            .getShows(page = page + 1, limit = pageSize)
            .let { showMapper.map(it) }
}
