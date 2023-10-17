// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.licenses.fetcher

import app.tivi.data.licenses.LicenseItem

interface LicensesFetcher {
  suspend operator fun invoke(): List<LicenseItem>
}
