// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.inject

import app.tivi.app.ApplicationInfo
import app.tivi.app.Flavor
import kotlin.experimental.ExperimentalNativeApi
import kotlinx.cinterop.ExperimentalForeignApi
import me.tatarka.inject.annotations.Provides
import platform.Foundation.NSBundle
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUserDomainMask

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
    cachePath = { NSFileManager.defaultManager.cacheDir },
    platform = app.tivi.app.Platform.IOS,
  )
}

@OptIn(ExperimentalForeignApi::class)
private val NSFileManager.cacheDir: String
  get() = URLForDirectory(
    directory = NSCachesDirectory,
    inDomain = NSUserDomainMask,
    appropriateForURL = null,
    create = true,
    error = null,
  )?.path.orEmpty()
