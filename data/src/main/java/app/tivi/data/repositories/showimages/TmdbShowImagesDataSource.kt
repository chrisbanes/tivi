/*
 * Copyright 2020 Google LLC
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

package app.tivi.data.repositories.showimages

import app.tivi.data.entities.ErrorResult
import app.tivi.data.entities.Result
import app.tivi.data.entities.ShowTmdbImage
import app.tivi.data.entities.TiviShow
import app.tivi.data.mappers.TmdbImagesToShowImages
import app.tivi.extensions.executeWithRetry
import app.tivi.extensions.toResult
import com.uwetrottmann.tmdb2.Tmdb
import com.uwetrottmann.tmdb2.entities.AppendToResponse
import com.uwetrottmann.tmdb2.enumerations.AppendToResponseItem
import javax.inject.Inject

class TmdbShowImagesDataSource @Inject constructor(
    private val tmdb: Tmdb,
    private val mapper: TmdbImagesToShowImages
) : ShowImagesDataSource {
    override suspend fun getShowImages(show: TiviShow): Result<List<ShowTmdbImage>> {
        val tmdbId = show.tmdbId
        return if (tmdbId != null) {
            tmdb.tvService().tv(tmdbId, null, AppendToResponse(AppendToResponseItem.IMAGES))
                .executeWithRetry()
                .toResult(mapper::map)
        } else {
            ErrorResult(IllegalArgumentException("TmdbId for show does not exist [$show]"))
        }
    }
}
