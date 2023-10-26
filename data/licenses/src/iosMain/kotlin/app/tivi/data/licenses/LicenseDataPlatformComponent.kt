// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.licenses

import app.tivi.data.licenses.fetcher.IosLicensesFetcherImpl
import app.tivi.data.licenses.fetcher.LicensesFetcher
import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Provides

actual interface LicenseDataPlatformComponent {

  @ApplicationScope
  @Provides
  fun bindLicensesFetcher(fetcher: IosLicensesFetcherImpl): LicensesFetcher = fetcher
}
