/*
 * Copyright 2018 Google, Inc.
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

package app.tivi.interactors

import app.tivi.data.daos.EntityInserter
import app.tivi.data.daos.UserDao
import app.tivi.data.entities.TraktUser
import app.tivi.extensions.fetchBodyWithRetry
import app.tivi.util.AppCoroutineDispatchers
import com.uwetrottmann.trakt5.entities.UserSlug
import com.uwetrottmann.trakt5.enums.Extended
import com.uwetrottmann.trakt5.services.Users
import kotlinx.coroutines.experimental.CoroutineDispatcher
import javax.inject.Inject
import javax.inject.Provider

class UpdateUserDetailsMe @Inject constructor(
    private val dao: UserDao,
    private val usersService: Provider<Users>,
    private val dispatchers: AppCoroutineDispatchers,
    private val entityInserter: EntityInserter
) : Interactor<UpdateUserDetailsMe.Params> {
    override val dispatcher: CoroutineDispatcher = dispatchers.io

    override suspend operator fun invoke(param: Params) {
        val response = usersService.get().profile(UserSlug.ME, Extended.FULL)
                .fetchBodyWithRetry()

        // Map to our entity
        val user = TraktUser(
                username = response.username,
                name = response.name,
                location = response.location,
                about = response.about,
                avatarUrl = response.images?.avatar?.full,
                joined = response.joined_at
        )

        dao.deleteAll()
        entityInserter.insertOrUpdate(dao, user)
    }

    data class Params(val forceLoad: Boolean)
}