// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

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
