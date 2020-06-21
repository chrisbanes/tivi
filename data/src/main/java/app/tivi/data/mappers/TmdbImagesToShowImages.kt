/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.tivi.data.mappers

import app.tivi.data.entities.ImageType
import app.tivi.data.entities.ShowTmdbImage
import com.uwetrottmann.tmdb2.entities.Image
import com.uwetrottmann.tmdb2.entities.TvShow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TmdbImagesToShowImages @Inject constructor() : Mapper<TvShow, List<ShowTmdbImage>> {
    override suspend fun map(from: TvShow): List<ShowTmdbImage> {
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
            from.poster_path?.also { path ->
                results += ShowTmdbImage(
                    showId = 0,
                    path = path,
                    type = ImageType.POSTER,
                    isPrimary = true
                )
            }
            from.backdrop_path?.also { path ->
                results += ShowTmdbImage(
                    showId = 0,
                    path = path,
                    type = ImageType.BACKDROP,
                    isPrimary = true
                )
            }
        }

        return results
    }

    private fun TvShow.mapImage(image: Image, type: ImageType) = ShowTmdbImage(
        showId = 0,
        path = image.file_path!!,
        type = type,
        language = image.iso_639_1,
        rating = image.vote_average?.toFloat() ?: 0f,
        isPrimary = when (type) {
            ImageType.BACKDROP -> image.file_path == backdrop_path
            ImageType.POSTER -> image.file_path == poster_path
            else -> false
        }
    )
}
