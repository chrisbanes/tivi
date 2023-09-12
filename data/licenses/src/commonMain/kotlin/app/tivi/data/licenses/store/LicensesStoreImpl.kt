// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.licenses.store

import app.tivi.data.licenses.LicenseItem
import app.tivi.data.licenses.fetcher.LicensesFetcher
import me.tatarka.inject.annotations.Inject

@Inject
class LicensesStoreImpl(private val fetcher: LicensesFetcher) : LicensesStore {

    override suspend fun getOpenSourceItemList(): List<LicenseItem> {
        return fetcher.fetch()
    }
}
