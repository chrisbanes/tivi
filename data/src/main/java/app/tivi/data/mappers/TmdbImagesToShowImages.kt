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
        fun mapImage(image: Image, type: ImageType): ShowTmdbImage {
            return ShowTmdbImage(
                    showId = 0,
                    path = image.file_path!!,
                    type = type,
                    language = image.iso_639_1,
                    rating = image.vote_average?.toFloat() ?: 0f,
                    isPrimary = when (type) {
                        ImageType.BACKDROP -> image.file_path == from.backdrop_path
                        ImageType.POSTER -> image.file_path == from.poster_path
                        else -> false
                    }
            )
        }

        val result = mutableListOf<ShowTmdbImage>()
        from.images?.posters?.mapTo(result) { mapImage(it, ImageType.POSTER) }
        from.images?.backdrops?.mapTo(result) { mapImage(it, ImageType.BACKDROP) }
        return result
    }
}
