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

package app.tivi.utils

import app.tivi.data.models.ShowTmdbImage
import app.tivi.data.models.TiviShow
import app.tivi.data.showimages.ShowImagesDataSource
import app.tivi.data.shows.ShowDataSource

object SuccessFakeShowDataSource : ShowDataSource {
    override suspend fun getShow(show: TiviShow): TiviShow = show
}

object SuccessFakeShowImagesDataSource : ShowImagesDataSource {
    override suspend fun getShowImages(show: TiviShow): List<ShowTmdbImage> {
        return listOf(showPoster)
    }
}
