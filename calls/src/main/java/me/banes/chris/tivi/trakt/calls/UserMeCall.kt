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
import com.uwetrottmann.trakt5.entities.User
import com.uwetrottmann.trakt5.entities.UserSlug
import com.uwetrottmann.trakt5.enums.Extended
import io.reactivex.Flowable
import io.reactivex.Single
import kotlinx.coroutines.experimental.rx2.await
import me.banes.chris.tivi.calls.Call
import me.banes.chris.tivi.data.daos.EntityInserter
import me.banes.chris.tivi.data.daos.UserDao
import me.banes.chris.tivi.data.entities.TraktUser
import me.banes.chris.tivi.extensions.toRxSingle
import me.banes.chris.tivi.util.AppRxSchedulers
import javax.inject.Inject

class UserMeCall @Inject constructor(
    private val dao: UserDao,
    private val trakt: TraktV2,
    private val schedulers: AppRxSchedulers,
    private val entityInserter: EntityInserter
) : Call<Unit, TraktUser> {

    override suspend fun refresh(param: Unit) {
        trakt.users().profile(UserSlug.ME, Extended.FULL).toRxSingle()
                .subscribeOn(schedulers.network)
                .flatMap(this::mapToOutput)
                .observeOn(schedulers.database)
                .doOnSuccess(this::saveEntry)
                .await()
    }

    fun data() = data(Unit)

    override fun data(param: Unit): Flowable<TraktUser> {
        return dao.getTraktUser()
                .subscribeOn(schedulers.database)
    }

    private fun mapToOutput(input: User): Single<TraktUser> {
        return Single.just(input)
                .map { networkUser ->
                    TraktUser(
                            username = networkUser.username,
                            name = networkUser.name,
                            location = networkUser.location,
                            about = networkUser.about,
                            avatarUrl = networkUser.images?.avatar?.full,
                            joined = networkUser.joined_at
                    )
                }
    }

    private fun saveEntry(user: TraktUser) {
        entityInserter.insertOrUpdate(dao, user)
    }
}
