// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.inject

import app.tivi.app.ApplicationInfo
import app.tivi.app.Flavor
import app.tivi.app.Platform
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.prefs.Preferences
import me.tatarka.inject.annotations.Provides
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient

actual interface SharedPlatformApplicationComponent {
  @ApplicationScope
  @Provides
  fun provideApplicationId(
    flavor: Flavor,
  ): ApplicationInfo = ApplicationInfo(
    packageName = "app.tivi",
    debugBuild = true,
    flavor = flavor,
    versionName = "1.0.0",
    versionCode = 1,
    cachePath = { getCacheDir().absolutePath },
    platform = Platform.DESKTOP,
  )

  @ApplicationScope
  @Provides
  fun providePreferences(): Preferences = Preferences.userRoot().node("app.tivi")

  @ApplicationScope
  @Provides
  fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
    // Adjust the Connection pool to account for historical use of 3 separate clients
    // but reduce the keepAlive to 2 minutes to avoid keeping radio open.
    .connectionPool(ConnectionPool(10, 2, TimeUnit.MINUTES))
    .dispatcher(
      Dispatcher().apply {
        // Allow for increased number of concurrent image fetches on same host
        maxRequestsPerHost = 10
      },
    )
    // Increase timeouts
    .connectTimeout(20, TimeUnit.SECONDS)
    .readTimeout(20, TimeUnit.SECONDS)
    .writeTimeout(20, TimeUnit.SECONDS)
    .build()
}

private fun getCacheDir(): File = when (currentOperatingSystem) {
  OperatingSystem.Windows -> File(System.getenv("AppData"), "tivi/cache")
  OperatingSystem.Linux -> File(System.getProperty("user.home"), ".cache/tivi")
  OperatingSystem.MacOS -> File(System.getProperty("user.home"), "Library/Caches/tivi")
  else -> throw IllegalStateException("Unsupported operating system")
}

internal enum class OperatingSystem {
  Windows,
  Linux,
  MacOS,
  Unknown,
}

private val currentOperatingSystem: OperatingSystem
  get() {
    val os = System.getProperty("os.name").lowercase()
    return when {
      os.contains("win") -> OperatingSystem.Windows
      os.contains("nix") || os.contains("nux") || os.contains("aix") -> {
        OperatingSystem.Linux
      }

      os.contains("mac") -> OperatingSystem.MacOS
      else -> OperatingSystem.Unknown
    }
  }
