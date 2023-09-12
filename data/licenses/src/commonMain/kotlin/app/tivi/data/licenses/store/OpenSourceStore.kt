// Copyright 2022, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.licenses.store

import app.tivi.data.licenses.LicensesState

interface OpenSourceStore {
    suspend fun fetch(): LicensesState
    fun save(state: LicensesState)
    fun clear()
    fun isAvailable(): Boolean = true
}
