// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.inject

import android.app.Application
import androidx.datastore.core.DataStore
import app.tivi.appinitializers.AppInitializers
import app.tivi.core.notifications.proto.PendingNotifications
import app.tivi.tasks.TiviWorkerFactory
import app.tivi.util.AppCoroutineDispatchers
import com.chuckerteam.chucker.api.ChuckerInterceptor
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor

@Component
@ApplicationScope
abstract class AndroidApplicationComponent(
  @get:Provides val application: Application,
) : SharedApplicationComponent,
  QaApplicationComponent {

  abstract val initializers: AppInitializers
  abstract val workerFactory: TiviWorkerFactory
  abstract val dispatchers: AppCoroutineDispatchers
  abstract val pendingNotificationsStore: DataStore<PendingNotifications>

  @ApplicationScope
  @IntoSet
  @Provides
  fun provideHttpLoggingInterceptor(): Interceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BASIC
  }

  @ApplicationScope
  @IntoSet
  @Provides
  fun provideChuckerInterceptor(
    context: Application,
  ): Interceptor = ChuckerInterceptor.Builder(context).build()

  companion object
}
