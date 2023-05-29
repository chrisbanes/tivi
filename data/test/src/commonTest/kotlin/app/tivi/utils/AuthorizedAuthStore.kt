// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

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
