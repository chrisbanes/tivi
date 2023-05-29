// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.traktusers

import app.moviebase.trakt.TraktExtended
import app.moviebase.trakt.api.TraktUsersApi
import app.moviebase.trakt.model.TraktUserSlug
import app.tivi.data.mappers.UserToTraktUser
import app.tivi.data.models.TraktUser
import me.tatarka.inject.annotations.Inject

@Inject
class TraktUsersDataSource(
    private val usersService: Lazy<TraktUsersApi>,
    private val mapper: UserToTraktUser,
) : UsersDataSource {

    override suspend fun getUser(slug: String): TraktUser =
        usersService.value
            .getProfile(TraktUserSlug(slug), TraktExtended.FULL)
            .let { mapper.map(it) }
}
