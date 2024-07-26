// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.entitlements

import app.tivi.appinitializers.AppInitializer
import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

actual interface EntitlementsPlatformComponent {
  @ApplicationScope
  @Provides
  fun bindEntitlementManager(impl: RevenueCatEntitlementManager): EntitlementManager = impl

  @Provides
  @IntoSet
  fun provideEntitlementManagerInitializer(impl: EntitlementManagerInitializer): AppInitializer = impl
}
