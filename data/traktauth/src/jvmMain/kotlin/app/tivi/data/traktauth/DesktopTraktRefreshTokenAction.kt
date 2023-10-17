// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.traktauth

import me.tatarka.inject.annotations.Inject

@Inject
class DesktopTraktRefreshTokenAction : TraktRefreshTokenAction {
  override suspend fun invoke(state: AuthState): AuthState? {
    // TODO
    return null
  }
}
