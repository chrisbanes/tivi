// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.notifications

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.dataStoreFile
import app.tivi.core.notifications.proto.PendingNotifications
import app.tivi.inject.ApplicationScope
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import me.tatarka.inject.annotations.Provides
import okio.Path.Companion.toOkioPath

actual interface NotificationPlatformComponent {

  @Provides
  @ApplicationScope
  fun provideNotificationManager(impl: AndroidNotificationManager): NotificationManager = impl

  @Provides
  @ApplicationScope
  fun providePendingNotificationsStore(
    application: Application,
    dispatchers: AppCoroutineDispatchers,
  ): DataStore<PendingNotifications> {
    val scope = CoroutineScope(dispatchers.io + SupervisorJob()) // Inject this
    return pendingNotificationsStore(scope) {
      application.dataStoreFile("pending_notifications.pb")
        .absoluteFile
        .toOkioPath()
    }
  }
}
