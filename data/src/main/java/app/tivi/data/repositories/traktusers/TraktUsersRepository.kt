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

package app.tivi.data.repositories.traktusers

import app.tivi.data.daos.UserDao
import app.tivi.data.entities.TraktUser
import app.tivi.data.withRetry
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import org.threeten.bp.Instant
import org.threeten.bp.Period

@Singleton
class TraktUsersRepository @Inject constructor(
    private val userDao: UserDao,
    private val lastRequestStore: TraktUsersLastRequestStore,
    private val traktDataSource: TraktUsersDataSource
) {
    fun observeUser(username: String): Flow<TraktUser?> = when (username) {
        "me" -> userDao.observeMe()
        else -> userDao.observeTraktUser(username)
    }

    suspend fun updateUser(username: String) {
        var user = withRetry {
            traktDataSource.getUser(username)
        }.let {
            // Tag the user as 'me' if that's what we're requesting
            if (username == "me") it.copy(isMe = true) else it
        }
        // Make sure we use the current DB id (if present)
        val localUser = userDao.getUser(user.username)
        if (localUser != null) {
            user = user.copy(id = localUser.id)
        }
        val id = userDao.insertOrUpdate(user)
        lastRequestStore.updateLastRequest(id, Instant.now())
    }

    suspend fun needUpdate(username: String): Boolean {
        return userDao.getIdForUsername(username)?.let { userId ->
            lastRequestStore.isRequestExpired(userId, Period.ofDays(7))
        } ?: true
    }
}
