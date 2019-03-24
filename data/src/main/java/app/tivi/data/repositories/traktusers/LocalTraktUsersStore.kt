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

import app.tivi.data.DatabaseTransactionRunner
import app.tivi.data.daos.EntityInserter
import app.tivi.data.daos.LastRequestDao
import app.tivi.data.daos.UserDao
import app.tivi.data.entities.Request
import app.tivi.data.entities.TraktUser
import org.threeten.bp.temporal.TemporalAmount
import javax.inject.Inject

class LocalTraktUsersStore @Inject constructor(
    private val entityInserter: EntityInserter,
    private val transactionRunner: DatabaseTransactionRunner,
    private val userDao: UserDao,
    private val lastRequestDao: LastRequestDao
) {
    fun observeUser(username: String) = when (username) {
        "me" -> userDao.observeMe()
        else -> userDao.observeTraktUser(username)
    }

    suspend fun getUser(username: String) = when (username) {
        "me" -> userDao.getMe()
        else -> userDao.getTraktUser(username)
    }

    suspend fun save(user: TraktUser) = transactionRunner {
        entityInserter.insertOrUpdate(userDao, user)
    }

    suspend fun updateLastRequest(username: String) {
        val id = when (username) {
            "me" -> userDao.getIdForMe()
            else -> userDao.getIdForUsername(username)
        }
        if (id != null) {
            lastRequestDao.updateLastRequest(Request.USER_PROFILE, id)
        }
    }

    suspend fun isLastRequestBefore(username: String, threshold: TemporalAmount): Boolean {
        val id = when (username) {
            "me" -> userDao.getIdForMe()
            else -> userDao.getIdForUsername(username)
        }
        return when {
            id != null -> lastRequestDao.isRequestBefore(Request.USER_PROFILE, id, threshold)
            else -> true
        }
    }
}