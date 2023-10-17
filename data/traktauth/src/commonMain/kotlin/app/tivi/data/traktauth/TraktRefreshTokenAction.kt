// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.traktauth

interface TraktRefreshTokenAction {
  suspend operator fun invoke(state: AuthState): AuthState?
}
