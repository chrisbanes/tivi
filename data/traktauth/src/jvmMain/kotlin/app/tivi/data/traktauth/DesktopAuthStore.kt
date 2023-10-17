// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.traktauth

import app.tivi.data.traktauth.store.AuthStore
import me.tatarka.inject.annotations.Inject

@Inject
class DesktopAuthStore : AuthStore {
  override suspend fun get(): AuthState? {
    // TODO no-op for now
    return null
  }

  override suspend fun save(state: AuthState) {
    // TODO no-op for now
  }

  override suspend fun clear() {
    // TODO no-op for now
  }
}
