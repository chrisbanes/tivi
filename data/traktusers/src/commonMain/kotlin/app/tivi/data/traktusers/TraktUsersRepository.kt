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

package app.tivi.data.traktusers

import app.tivi.data.daos.UserDao
import app.tivi.data.db.DatabaseTransactionRunner
import app.tivi.data.models.TraktUser
import app.tivi.inject.ApplicationScope
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import me.tatarka.inject.annotations.Inject

@ApplicationScope
@Inject
class TraktUsersRepository(
    private val userDao: UserDao,
    private val lastRequestStore: TraktUsersLastRequestStore,
    private val dataSource: UsersDataSource,
    private val transactionRunner: DatabaseTransactionRunner,
) {
    fun observeUser(username: String): Flow<TraktUser?> = when (username) {
        "me" -> userDao.observeMe()
        else -> userDao.observeTraktUser(username)
    }

    suspend fun updateUser(username: String) {
        var user = dataSource.getUser(username).let {
            // Tag the user as 'me' if that's what we're requesting
            if (username == "me") {
                it.copy(isMe = true)
            } else {
                it
            }
        }
        // Make sure we use the current DB id (if present)
        transactionRunner {
            val localUser = userDao.getUser(user.username)
            if (localUser != null) {
                user = user.copy(id = localUser.id)
            }
            val id = userDao.upsert(user)
            lastRequestStore.updateLastRequest(id, Clock.System.now())
        }
    }

    fun needUpdate(username: String): Boolean {
        return userDao.getId(username)?.let { userId ->
            lastRequestStore.isRequestExpired(userId, 7.days)
        } ?: true
    }
}
