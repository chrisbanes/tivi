// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.inject

import android.app.Application
import android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE
import app.tivi.app.ApplicationInfo
import app.tivi.app.Flavor
import app.tivi.app.Platform
import app.tivi.core.notifications.PendingNotificationStore
import app.tivi.tasks.TiviWorkerFactory
import java.io.File
import java.util.concurrent.TimeUnit
import me.tatarka.inject.annotations.Provides
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.Interceptor
import okhttp3.OkHttpClient

actual interface SharedPlatformApplicationComponent {

  val workerFactory: TiviWorkerFactory
  val pendingNotificationsStore: PendingNotificationStore

  @ApplicationScope
  @Provides
  fun provideApplicationInfo(
    application: Application,
    flavor: Flavor,
  ): ApplicationInfo {
    val packageManager = application.packageManager
    val applicationInfo = packageManager.getApplicationInfo(application.packageName, 0)
    val packageInfo = packageManager.getPackageInfo(application.packageName, 0)

    return ApplicationInfo(
      packageName = application.packageName,
      debugBuild = (applicationInfo.flags and FLAG_DEBUGGABLE) != 0,
      flavor = flavor,
      versionName = packageInfo.versionName,
      versionCode = @Suppress("DEPRECATION") packageInfo.versionCode,
      cachePath = { application.cacheDir.absolutePath },
      platform = Platform.ANDROID,
    )
  }

  @ApplicationScope
  @Provides
  fun provideOkHttpClient(
    context: Application,
    interceptors: Set<Interceptor>,
  ): OkHttpClient = OkHttpClient.Builder()
    .apply { interceptors.forEach(::addInterceptor) }
    // Around 4¢ worth of storage in 2020
    .cache(Cache(File(context.cacheDir, "api_cache"), 50L * 1024 * 1024))
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
