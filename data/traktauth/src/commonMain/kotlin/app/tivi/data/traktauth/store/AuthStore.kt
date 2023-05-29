// Copyright 2022, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.traktauth.store

import app.tivi.data.traktauth.AuthState

interface AuthStore {
    suspend fun get(): AuthState?
    suspend fun save(state: AuthState)
    suspend fun clear()
    suspend fun isAvailable(): Boolean = true
}
