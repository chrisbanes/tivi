// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.utils

import app.tivi.data.traktauth.AuthState
import app.tivi.data.traktauth.TraktLoginAction
import app.tivi.data.traktauth.TraktRefreshTokenAction
import app.tivi.util.Logger

object SuccessTraktLoginAction : TraktLoginAction {
  override suspend fun invoke(): AuthState = AuthorizedAuthState
}

object SuccessRefreshTokenAction : TraktRefreshTokenAction {
  override suspend fun invoke(state: AuthState): AuthState = AuthorizedAuthState
}

object FakeLogger : Logger
