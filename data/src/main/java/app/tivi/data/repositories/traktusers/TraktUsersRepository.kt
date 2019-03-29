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

import app.tivi.data.entities.Success
import org.threeten.bp.Period
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraktUsersRepository @Inject constructor(
    private val localStore: LocalTraktUsersStore,
    private val traktDataSource: TraktUsersDataSource
) {
    fun observeUser(username: String) = localStore.observeUser(username)

    suspend fun updateUser(username: String) {
        val response = traktDataSource.getUser(username)
        when {
            response is Success && response.responseModified -> {
                var user = response.data
                // Tag the user as 'me' if that's what we're requesting
                if (username == "me") {
                    user = user.copy(isMe = true)
                }
                // Make sure we use the current DB id (if present)
                val localUser = localStore.getUser(user.username)
                if (localUser != null) {
                    user = user.copy(id = localUser.id)
                }
                localStore.save(user)
                localStore.updateLastRequest(username)
            }
        }
    }

    suspend fun needUpdate(username: String): Boolean = localStore.isLastRequestBefore(username, Period.ofDays(7))
}