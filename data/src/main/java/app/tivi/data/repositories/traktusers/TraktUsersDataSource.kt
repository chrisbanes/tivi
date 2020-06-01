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

import app.tivi.data.entities.Result
import app.tivi.data.entities.TraktUser
import app.tivi.data.mappers.UserToTraktUser
import app.tivi.extensions.executeWithRetry
import app.tivi.extensions.toResult
import com.uwetrottmann.trakt5.entities.UserSlug
import com.uwetrottmann.trakt5.enums.Extended
import com.uwetrottmann.trakt5.services.Users
import javax.inject.Inject
import javax.inject.Provider

class TraktUsersDataSource @Inject constructor(
    private val usersService: Provider<Users>,
    private val mapper: UserToTraktUser
) {
    suspend fun getUser(slug: String): Result<TraktUser> {
        return usersService.get().profile(UserSlug(slug), Extended.FULL)
            .executeWithRetry()
            .toResult(mapper::map)
    }
}
