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

package me.banes.chris.tivi.calls

import com.uwetrottmann.trakt5.TraktV2
import com.uwetrottmann.trakt5.entities.User
import com.uwetrottmann.trakt5.entities.UserSlug
import com.uwetrottmann.trakt5.enums.Extended
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import me.banes.chris.tivi.data.TraktUser
import me.banes.chris.tivi.data.UserDao
import me.banes.chris.tivi.extensions.toRxSingle
import me.banes.chris.tivi.util.AppRxSchedulers
import me.banes.chris.tivi.util.DatabaseTxRunner
import javax.inject.Inject

class UserMeCall @Inject constructor(
        private val dbTxRunner: DatabaseTxRunner,
        private val dao: UserDao,
        private val trakt: TraktV2,
        schedulers: AppRxSchedulers)
    : TraktCallImpl<User, TraktUser>(schedulers) {

    override fun networkCall(): Single<User> {
        return trakt.users().profile(UserSlug.ME, Extended.FULL).toRxSingle()
    }

    override fun createDatabaseObservable(): Flowable<TraktUser> {
        return dao.getTraktUser()
                .subscribeOn(schedulers.disk)
    }

    override fun mapToOutput(input: User): Maybe<TraktUser> {
        return Maybe.just(input)
                .map { networkUser ->
                    TraktUser(
                            username = networkUser.username,
                            name = networkUser.name,
                            location = networkUser.location,
                            about = networkUser.about,
                            avatarUrl = networkUser.images?.avatar?.full
                    )
                }
    }

    override fun saveEntry(show: TraktUser) {
        dao.insertUser(show)
    }
}
