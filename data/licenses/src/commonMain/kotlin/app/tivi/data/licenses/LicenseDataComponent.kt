// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.licenses

import app.tivi.data.licenses.store.LicensesStore
import app.tivi.data.licenses.store.LicensesStoreImpl
import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Provides

expect interface LicenseDataPlatformComponent

interface LicenseDataComponent : LicenseDataPlatformComponent {
  @ApplicationScope
  @Provides
  fun bindLicensesStore(bind: LicensesStoreImpl): LicensesStore = bind
}
