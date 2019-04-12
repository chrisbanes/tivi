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

import app.tivi.data.entities.TraktUser
import com.uwetrottmann.trakt5.entities.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserToTraktUser @Inject constructor() : Mapper<User, TraktUser> {
    override suspend fun map(from: User) = TraktUser(
            username = from.username,
            name = from.name,
            location = from.location,
            about = from.about,
            avatarUrl = from.images?.avatar?.full,
            joined = from.joined_at,
            vip = from.vip
    )
}