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

package app.tivi.data.daos

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.tivi.data.Database
import app.tivi.data.await
import app.tivi.data.models.ShowTmdbImage
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class SqlDelightShowImagesDao(
    private val db: Database,
    private val dispatchers: AppCoroutineDispatchers,
) : ShowTmdbImagesDao {
    override suspend fun deleteForShowId(showId: Long) = withContext(dispatchers.io) {
        db.show_imagesQueries.deleteForShowId(showId)
    }

    override suspend fun imageCountForShowId(showId: Long): Int {
        return db.show_imagesQueries.getImageCountForShowId(showId)
            .await(dispatchers.io)
            .toInt()
    }

    override fun getImagesForShowId(showId: Long): Flow<List<ShowTmdbImage>> {
        return db.show_imagesQueries.getImagesForShowId(showId, ::ShowTmdbImage)
            .asFlow()
            .mapToList(dispatchers.io)
    }

    override suspend fun deleteAll() = withContext(dispatchers.io) {
        db.show_imagesQueries.deleteAll()
    }

    override suspend fun upsert(entity: ShowTmdbImage): Long = withContext(dispatchers.io) {
        db.transactionWithResult {
            upsertBlocking(entity)
            db.show_imagesQueries.lastInsertRowId().executeAsOne()
        }
    }

    override suspend fun upsertAll(entities: List<ShowTmdbImage>) = withContext(dispatchers.io) {
        db.transaction {
            entities.forEach(::upsertBlocking)
        }
    }

    override suspend fun deleteEntity(entity: ShowTmdbImage) = withContext(dispatchers.io) {
        db.show_imagesQueries.delete(entity.id)
    }

    private fun upsertBlocking(entity: ShowTmdbImage) {
        db.show_imagesQueries.upsert(
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
