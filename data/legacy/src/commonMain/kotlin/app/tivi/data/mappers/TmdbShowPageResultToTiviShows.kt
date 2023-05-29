// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.mappers

import app.moviebase.tmdb.model.TmdbShowPageResult
import app.tivi.data.models.ImageType
import app.tivi.data.models.ShowTmdbImage
import app.tivi.data.models.TiviShow
import me.tatarka.inject.annotations.Inject

@Inject
class TmdbShowPageResultToTiviShows(
    private val tmdbShowMapper: TmdbShowToTiviShow,
) : Mapper<TmdbShowPageResult, List<Pair<TiviShow, List<ShowTmdbImage>>>> {
    override fun map(from: TmdbShowPageResult): List<Pair<TiviShow, List<ShowTmdbImage>>> {
        return from.results.map { result ->
            val show = tmdbShowMapper.map(result)

            val images = ArrayList<ShowTmdbImage>()
            result.posterPath?.let { path ->
                images += ShowTmdbImage(
                    showId = 0,
                    path = path,
                    isPrimary = true,
                    type = ImageType.POSTER,
                )
            }
            result.backdropPath?.let { path ->
                images += ShowTmdbImage(
                    showId = 0,
                    path = path,
                    isPrimary = true,
                    type = ImageType.BACKDROP,
                )
            }
            show to images
        }
    }
}
