// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.showimages

import app.moviebase.tmdb.Tmdb3
import app.moviebase.tmdb.model.AppendResponse
import app.tivi.data.mappers.TmdbShowDetailToShowImages
import app.tivi.data.models.ShowTmdbImage
import app.tivi.data.models.TiviShow
import me.tatarka.inject.annotations.Inject

@Inject
class TmdbShowImagesDataSource(
    private val tmdb: Tmdb3,
    private val mapper: TmdbShowDetailToShowImages,
) : ShowImagesDataSource {
    override suspend fun getShowImages(show: TiviShow): List<ShowTmdbImage> {
        val tmdbId = show.tmdbId
            ?: throw IllegalArgumentException("TmdbId for show does not exist [$show]")

        return tmdb.show
            .getDetails(showId = tmdbId, appendResponses = listOf(AppendResponse.IMAGES))
            .let { mapper.map(it) }
    }
}
