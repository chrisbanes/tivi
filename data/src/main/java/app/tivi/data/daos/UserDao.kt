/*
 * Copyright 2017 Google LLC
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

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.tivi.data.entities.TraktUser
import kotlinx.coroutines.flow.Flow

@Dao
abstract class UserDao : EntityDao<TraktUser>() {
    @Query("SELECT * FROM users WHERE is_me != 0")
    abstract fun observeMe(): Flow<TraktUser?>

    @Query("SELECT * FROM users WHERE username = :username")
    abstract fun observeTraktUser(username: String): Flow<TraktUser?>

    @Query("SELECT * FROM users WHERE username = :username")
    abstract suspend fun getTraktUser(username: String): TraktUser?

    @Query("SELECT * FROM users WHERE is_me != 0")
    abstract suspend fun getMe(): TraktUser?

    @Query("SELECT id FROM users WHERE username = :username")
    abstract suspend fun getIdForUsername(username: String): Long?

    @Query("SELECT id FROM users WHERE is_me != 0")
    abstract suspend fun getIdForMe(): Long?

    @Query("DELETE FROM users WHERE username = :username")
    abstract suspend fun deleteWithUsername(username: String)

    @Query("DELETE FROM users WHERE is_me != 0")
    abstract suspend fun deleteMe()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract override suspend fun insert(entity: TraktUser): Long

    @Query("DELETE FROM users")
    abstract suspend fun deleteAll()
}
