// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.licenses.store

import app.tivi.data.licenses.LicenseItem
import me.tatarka.inject.annotations.Inject

@Inject
interface LicensesStore {
    suspend fun getOpenSourceItemList(): List<LicenseItem>
}
