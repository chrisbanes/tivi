/*
 * Copyright 2018 Google LLC
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

import app.tivi.data.models.ImageType
import app.tivi.data.models.ShowTmdbImage
import app.tivi.data.models.TiviShow
import app.tivi.inject.ApplicationScope
import com.uwetrottmann.tmdb2.entities.TvShowResultsPage
import javax.inject.Inject

@ApplicationScope
class TmdbShowResultsPageToTiviShows @Inject constructor(
    private val tmdbShowMapper: TmdbBaseShowToTiviShow,
) : Mapper<TvShowResultsPage, List<Pair<TiviShow, List<ShowTmdbImage>>>> {
    override suspend fun map(from: TvShowResultsPage): List<Pair<TiviShow, List<ShowTmdbImage>>> {
        return from.results.map {
            val show = tmdbShowMapper.map(it)

            val images = ArrayList<ShowTmdbImage>()
            if (it.poster_path != null) {
                images += ShowTmdbImage(
                    showId = 0,
                    path = it.poster_path,
                    isPrimary = true,
                    type = ImageType.POSTER,
                )
            }
            if (it.backdrop_path != null) {
                images += ShowTmdbImage(
                    showId = 0,
                    path = it.backdrop_path,
                    isPrimary = true,
                    type = ImageType.BACKDROP,
                )
            }
            show to images
        }
    }
}
