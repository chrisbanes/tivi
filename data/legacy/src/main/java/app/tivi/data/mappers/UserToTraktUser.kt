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

package app.tivi.data.mappers

import app.tivi.data.models.TraktUser
import app.tivi.data.util.toKotlinInstant
import me.tatarka.inject.annotations.Inject
import app.moviebase.trakt.model.TraktUser as ApiTraktUser

@Inject
class UserToTraktUser() : Mapper<ApiTraktUser, TraktUser> {

    override suspend fun map(from: ApiTraktUser) = TraktUser(
        username = from.userName!!,
        name = from.name,
        location = from.location,
        about = from.about,
        avatarUrl = from.images?.avatar?.full,
        joined = from.joinedAt,
        vip = from.vip,
    )
}
