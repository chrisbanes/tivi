// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.mappers

import app.moviebase.tmdb.model.TmdbFileImage
import app.moviebase.tmdb.model.TmdbShowDetail
import app.tivi.data.models.ImageType
import app.tivi.data.models.ShowTmdbImage
import me.tatarka.inject.annotations.Inject

@Inject
class TmdbShowDetailToShowImages : Mapper<TmdbShowDetail, List<ShowTmdbImage>> {
    override fun map(from: TmdbShowDetail): List<ShowTmdbImage> {
        val results = ArrayList<ShowTmdbImage>()
        from.images?.posters?.mapTo(results) { image ->
            from.mapImage(image, ImageType.POSTER)
        }
        from.images?.backdrops?.mapTo(results) { image ->
            from.mapImage(image, ImageType.BACKDROP)
        }

        if (results.isEmpty()) {
            // If we have no images, we haven't been passed a result which has images. We'll
            // synthesize some from the TvShow properties
            from.posterPath?.let { path ->
                results += ShowTmdbImage(
                    showId = 0,
                    path = path,
                    isPrimary = true,
                    type = ImageType.POSTER,
                )
            }
            from.backdropPath?.let { path ->
                results += ShowTmdbImage(
                    showId = 0,
                    path = path,
                    isPrimary = true,
                    type = ImageType.BACKDROP,
                )
            }
        }

        return results
    }

    private fun TmdbShowDetail.mapImage(image: TmdbFileImage, type: ImageType) = ShowTmdbImage(
        showId = 0,
        path = image.filePath,
        type = type,
        language = image.iso639,
        rating = image.voteAverage ?: 0f,
        isPrimary = when (type) {
            ImageType.BACKDROP -> image.filePath == backdropPath
            ImageType.POSTER -> image.filePath == posterPath
            else -> false
        },
    )
}
