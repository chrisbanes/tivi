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

package app.tivi.data.compoundmodels

import app.tivi.data.models.Episode
import app.tivi.data.models.ImageType
import app.tivi.data.models.Season
import app.tivi.data.models.TiviShow
import app.tivi.data.models.TmdbImageEntity
import app.tivi.data.util.findHighestRatedItem
import app.tivi.extensions.unsafeLazy

data class EpisodeWithSeasonWithShow(
    val episode: Episode,
    val season: Season,
    val show: TiviShow,
    private val showImages: List<TmdbImageEntity>,
) {
    val backdrop by unsafeLazy { findHighestRatedItem(showImages, ImageType.BACKDROP) }
    val poster by unsafeLazy { findHighestRatedItem(showImages, ImageType.POSTER) }
}
