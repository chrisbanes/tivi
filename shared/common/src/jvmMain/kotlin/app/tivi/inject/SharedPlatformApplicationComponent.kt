// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.inject

import androidx.compose.ui.unit.Density
import app.tivi.app.ApplicationInfo
import app.tivi.app.Flavor
import java.util.concurrent.TimeUnit
import java.util.prefs.Preferences
import me.tatarka.inject.annotations.Provides
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient

@OptIn(ExperimentalMultiplatform::class)
@AllowDifferentMembersInActual
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
  )

  @ApplicationScope
  @Provides
  fun providePreferences(): Preferences = Preferences.userRoot().node("app.tivi")

  @Provides
  fun provideDensity(): Density = Density(density = 1f) // FIXME

  @ApplicationScope
  @Provides
  fun provideOkHttpClient(
    // interceptors: Set<Interceptor>,
  ): OkHttpClient = OkHttpClient.Builder()
    // .apply { interceptors.forEach(::addInterceptor) }
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
