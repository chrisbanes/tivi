// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.mappers

import app.moviebase.trakt.model.TraktUser as ApiTraktUser
import app.tivi.data.models.TraktUser
import me.tatarka.inject.annotations.Inject

@Inject
class UserToTraktUser : Mapper<ApiTraktUser, TraktUser> {

    override fun map(from: ApiTraktUser) = TraktUser(
        username = from.userName!!,
        name = from.name,
        location = from.location,
        about = from.about,
        avatarUrl = from.images?.avatar?.full,
        joined = from.joinedAt,
        vip = from.vip,
    )
}
