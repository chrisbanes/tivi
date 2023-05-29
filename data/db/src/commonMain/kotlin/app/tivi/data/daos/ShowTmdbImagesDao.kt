// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.daos

import app.tivi.data.models.ShowTmdbImage
import kotlinx.coroutines.flow.Flow

interface ShowTmdbImagesDao : EntityDao<ShowTmdbImage> {

    fun deleteForShowId(showId: Long)

    fun imageCountForShowId(showId: Long): Int

    fun getImagesForShowId(showId: Long): Flow<List<ShowTmdbImage>>

    fun deleteAll()
}

fun ShowTmdbImagesDao.saveImages(showId: Long, images: List<ShowTmdbImage>) {
    deleteForShowId(showId)
    upsert(images)
}

fun ShowTmdbImagesDao.saveImagesIfEmpty(showId: Long, images: List<ShowTmdbImage>) {
    if (imageCountForShowId(showId) <= 0) {
        upsert(images)
    }
}
