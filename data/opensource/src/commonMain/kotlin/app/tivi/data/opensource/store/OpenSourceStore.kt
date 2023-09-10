// Copyright 2022, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.opensource.store

import app.tivi.data.opensource.OpenSourceState

interface OpenSourceStore {
    fun fetch(): OpenSourceState
    fun save(state: OpenSourceState)
    fun clear()
    fun isAvailable(): Boolean = true
}
