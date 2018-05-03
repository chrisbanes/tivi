/*
 * Copyright 2017 Google, Inc.
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

package me.banes.chris.tivi.trakt.calls

import com.uwetrottmann.trakt5.TraktV2
import com.uwetrottmann.trakt5.entities.UserSlug
import com.uwetrottmann.trakt5.enums.Extended
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.reactive.openSubscription
import kotlinx.coroutines.experimental.withContext
import me.banes.chris.tivi.calls.Call
import me.banes.chris.tivi.data.daos.EntityInserter
import me.banes.chris.tivi.data.daos.UserDao
import me.banes.chris.tivi.data.entities.TraktUser
import me.banes.chris.tivi.extensions.fetchBodyWithRetry
import me.banes.chris.tivi.util.AppCoroutineDispatchers
import me.banes.chris.tivi.util.AppRxSchedulers
import javax.inject.Inject

class UserMeCall @Inject constructor(
    private val dao: UserDao,
    private val trakt: TraktV2,
    private val schedulers: AppRxSchedulers,
    private val dispatchers: AppCoroutineDispatchers,
    private val entityInserter: EntityInserter
) : Call<Unit, TraktUser> {

    override suspend fun refresh(param: Unit) {
        // Fetch network response on network dispatcher
        val networkResponse = withContext(dispatchers.network) {
            trakt.users().profile(UserSlug.ME, Extended.FULL).fetchBodyWithRetry()
        }

        networkResponse.let {
            // Map to our entity
            TraktUser(
                    username = it.username,
                    name = it.name,
                    location = it.location,
                    about = it.about,
                    avatarUrl = it.images?.avatar?.full,
                    joined = it.joined_at
            )
        }.let {
            // Save to the database on the database dispatcher
            withContext(dispatchers.database) {
                entityInserter.insertOrUpdate(dao, it)
            }
        }
    }

    fun data() = data(Unit)

    override fun data(param: Unit): ReceiveChannel<TraktUser> {
        return dao.getTraktUser()
                .subscribeOn(schedulers.database)
                .distinctUntilChanged()
                .openSubscription()
    }
}
