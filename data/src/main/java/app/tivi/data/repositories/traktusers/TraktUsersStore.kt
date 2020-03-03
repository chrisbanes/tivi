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
import javax.inject.Inject

class TraktUsersStore @Inject constructor(
    private val userDao: UserDao
) {
    fun observeUser(username: String) = when (username) {
        "me" -> userDao.observeMe()
        else -> userDao.observeTraktUser(username)
    }

    suspend fun getUser(username: String) = when (username) {
        "me" -> userDao.getMe()
        else -> userDao.getTraktUser(username)
    }

    suspend fun getIdForUsername(username: String) = when (username) {
        "me" -> userDao.getIdForMe()
        else -> userDao.getIdForUsername(username)
    }

    suspend fun save(user: TraktUser) = userDao.insertOrUpdate(user)
}
