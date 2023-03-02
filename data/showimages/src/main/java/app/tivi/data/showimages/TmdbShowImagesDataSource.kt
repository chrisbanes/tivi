/*
 * Copyright 2023 Google LLC
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

package app.tivi.data.showimages

import app.moviebase.tmdb.Tmdb3
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

        return tmdb.show.getDetails(tmdbId)
            .let { mapper.map(it) }
    }
}
