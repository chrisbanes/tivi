/*
 * Copyright 2023 Google LLC
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

package app.tivi.utils

import app.tivi.data.traktauth.AuthState
import app.tivi.data.traktauth.store.AuthStore

object AuthorizedAuthStore : AuthStore {
    override suspend fun get(): AuthState = AuthorizedAuthState
    override suspend fun save(state: AuthState) = Unit
    override suspend fun clear() = Unit
}

object AuthorizedAuthState : AuthState {
    override val accessToken: String = "access-token"
    override val refreshToken: String = "refresh-token"
    override val isAuthorized: Boolean = true
    override fun serializeToJson(): String = "{}"
}
