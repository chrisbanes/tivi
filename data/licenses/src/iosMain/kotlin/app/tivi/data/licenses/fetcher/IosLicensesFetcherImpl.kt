// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.licenses.fetcher

import app.tivi.data.licenses.LicenseItem
import me.tatarka.inject.annotations.Inject

@Inject
class IosLicensesFetcherImpl : LicensesFetcher {
    override suspend fun fetch(): List<LicenseItem> {
        return emptyList()
    }
}
