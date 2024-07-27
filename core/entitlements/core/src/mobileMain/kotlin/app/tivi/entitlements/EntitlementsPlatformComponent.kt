// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.entitlements

import app.tivi.app.ApplicationInfo
import app.tivi.app.Flavor
import app.tivi.appinitializers.AppInitializer
import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

actual interface EntitlementsPlatformComponent {
  @ApplicationScope
  @Provides
  fun provideEntitlementManager(
    applicationInfo: ApplicationInfo,
    revenueCatImpl: () -> RevenueCatEntitlementManager,
  ): EntitlementManager = when (applicationInfo.flavor) {
    // QA builds use different package/bundle ids so we can't use IAPs. Assume that QA == Pro
    Flavor.Qa -> EntitlementManager.Always
    // For standard build, we can use IAPs
    Flavor.Standard -> revenueCatImpl()
  }

  @Provides
  @IntoSet
  fun provideEntitlementManagerInitializer(impl: EntitlementManagerInitializer): AppInitializer = impl
}
