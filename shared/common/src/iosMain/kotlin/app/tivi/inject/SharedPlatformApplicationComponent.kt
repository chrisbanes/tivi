// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.inject

import app.tivi.app.ApplicationInfo
import app.tivi.app.Flavor
import kotlin.experimental.ExperimentalNativeApi
import me.tatarka.inject.annotations.Provides
import platform.Foundation.NSBundle
import platform.Foundation.NSUserDefaults

actual interface SharedPlatformApplicationComponent {
  @Provides
  fun provideNsUserDefaults(): NSUserDefaults = NSUserDefaults.standardUserDefaults

  @OptIn(ExperimentalNativeApi::class)
  @ApplicationScope
  @Provides
  fun provideApplicationId(
    flavor: Flavor,
  ): ApplicationInfo = ApplicationInfo(
    packageName = NSBundle.mainBundle.bundleIdentifier ?: error("Bundle ID not found"),
    debugBuild = Platform.isDebugBinary,
    flavor = flavor,
    versionName = NSBundle.mainBundle.infoDictionary
      ?.get("CFBundleShortVersionString") as? String
      ?: "",
    versionCode = (NSBundle.mainBundle.infoDictionary?.get("CFBundleVersion") as? String)
      ?.toIntOrNull()
      ?: 0,
  )
}
