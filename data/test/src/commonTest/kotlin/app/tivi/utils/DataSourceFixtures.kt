// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

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
