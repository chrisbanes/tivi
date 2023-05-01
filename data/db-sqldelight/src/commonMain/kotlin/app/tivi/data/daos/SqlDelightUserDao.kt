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
import app.cash.sqldelight.coroutines.mapToOne
import app.tivi.data.Database
import app.tivi.data.awaitAsNull
import app.tivi.data.models.TraktUser
import app.tivi.data.upsert
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class SqlDelightUserDao(
    override val db: Database,
    override val dispatchers: AppCoroutineDispatchers,
) : UserDao, SqlDelightEntityDao<TraktUser> {
    override fun upsertBlocking(entity: TraktUser): Long {
        return db.usersQueries.upsert(
            entity = entity,
            insert = { entity ->
                insert(
                    id = entity.id,
                    username = entity.username,
                    name = entity.name,
                    joined_date = entity.joined,
                    location = entity.location,
                    about = entity.about,
                    avatar_url = entity.avatarUrl,
                    vip = entity.vip,
                    is_me = entity.isMe,
                )
            },
            update = { entity ->
                update(
                    id = entity.id,
                    username = entity.username,
                    name = entity.name,
                    joined_date = entity.joined,
                    location = entity.location,
                    about = entity.about,
                    avatar_url = entity.avatarUrl,
                    vip = entity.vip,
                    is_me = entity.isMe,
                )
            },
            lastInsertRowId = { lastInsertRowId().executeAsOne() },
        )
    }

    override fun observeMe(): Flow<TraktUser?> {
        return db.usersQueries.getEntryForMe(::TraktUser).asFlow().mapToOne(dispatchers.io)
    }

    override fun observeTraktUser(username: String): Flow<TraktUser?> {
        return db.usersQueries.getEntryForUsername(username, ::TraktUser)
            .asFlow().mapToOne(dispatchers.io)
    }

    override suspend fun getUser(username: String): TraktUser? = when (username) {
        "me" -> db.usersQueries.getEntryForMe(::TraktUser).awaitAsNull(dispatchers.io)
        else -> {
            db.usersQueries.getEntryForUsername(username, ::TraktUser)
                .awaitAsNull(dispatchers.io)
        }
    }

    override suspend fun getId(username: String): Long? = when (username) {
        "me" -> db.usersQueries.idForMe().awaitAsNull(dispatchers.io)
        else -> db.usersQueries.idForUsername(username).awaitAsNull(dispatchers.io)
    }

    override suspend fun deleteWithUsername(username: String): Unit = withContext(dispatchers.io) {
        db.usersQueries.deleteWithUsername(username)
    }

    override suspend fun deleteMe(): Unit = withContext(dispatchers.io) {
        db.usersQueries.deleteMe()
    }

    override suspend fun deleteAll(): Unit = withContext(dispatchers.io) {
        db.usersQueries.deleteAll()
    }

    override suspend fun deleteEntity(entity: TraktUser): Unit = withContext(dispatchers.io) {
        db.usersQueries.deleteWithId(entity.id)
    }
}
