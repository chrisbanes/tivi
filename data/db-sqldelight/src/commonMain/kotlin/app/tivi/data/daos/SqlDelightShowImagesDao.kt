// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.daos

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.tivi.data.Database
import app.tivi.data.models.ShowTmdbImage
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
class SqlDelightShowImagesDao(
    override val db: Database,
    private val dispatchers: AppCoroutineDispatchers,
) : ShowTmdbImagesDao, SqlDelightEntityDao<ShowTmdbImage> {
    override fun deleteForShowId(showId: Long) {
        db.show_imagesQueries.deleteForShowId(showId)
    }

    override fun imageCountForShowId(showId: Long): Int {
        return db.show_imagesQueries.getImageCountForShowId(showId)
            .executeAsOne()
            .toInt()
    }

    override fun getImagesForShowId(showId: Long): Flow<List<ShowTmdbImage>> {
        return db.show_imagesQueries.getImagesForShowId(showId, ::ShowTmdbImage)
            .asFlow()
            .mapToList(dispatchers.io)
    }

    override fun deleteAll() {
        db.show_imagesQueries.deleteAll()
    }

    override fun deleteEntity(entity: ShowTmdbImage) {
        db.show_imagesQueries.delete(entity.id)
    }

    override fun insert(entity: ShowTmdbImage): Long {
        db.show_imagesQueries.insert(
            id = entity.id,
            show_id = entity.showId,
            path = entity.path,
            type = entity.type,
            lang = entity.language,
            rating = entity.rating,
            is_primary = entity.isPrimary,
        )
        return db.show_imagesQueries.lastInsertRowId().executeAsOne()
    }

    override fun update(entity: ShowTmdbImage) {
        db.show_imagesQueries.update(
            id = entity.id,
            show_id = entity.showId,
            path = entity.path,
            type = entity.type,
            lang = entity.language,
            rating = entity.rating,
            is_primary = entity.isPrimary,
        )
    }
}
