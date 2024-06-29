// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi

import android.app.Application
import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import androidx.work.Configuration
import androidx.work.WorkerFactory
import app.tivi.core.notifications.PendingNotificationStore
import app.tivi.core.notifications.PendingNotificationsStoreProvider
import app.tivi.extensions.unsafeLazy
import app.tivi.inject.AndroidApplicationComponent
import app.tivi.inject.create

class TiviApplication :
  Application(),
  Configuration.Provider,
  PendingNotificationsStoreProvider {
  val component: AndroidApplicationComponent by unsafeLazy {
    AndroidApplicationComponent.create(this)
  }

  private lateinit var workerFactory: WorkerFactory

  override val pendingNotificationsStore: PendingNotificationStore
    get() = component.pendingNotificationsStore

  override fun onCreate() {
    super.onCreate()
    setupStrictMode()
    workerFactory = component.workerFactory
    component.initializers.initialize()
  }

  override val workManagerConfiguration: Configuration
    get() = Configuration.Builder()
      .setWorkerFactory(workerFactory)
      .build()
}

private fun setupStrictMode() {
  StrictMode.setThreadPolicy(
    ThreadPolicy.Builder()
      .detectAll()
      .penaltyLog()
      .build(),
  )
  StrictMode.setVmPolicy(
    VmPolicy.Builder()
      .detectLeakedSqlLiteObjects()
      .detectActivityLeaks()
      .detectLeakedClosableObjects()
      .detectLeakedRegistrationObjects()
      .detectFileUriExposure()
      .detectCleartextNetwork()
      .apply {
        if (Build.VERSION.SDK_INT >= 26) {
          detectContentUriWithoutPermission()
        }
        if (Build.VERSION.SDK_INT >= 29) {
          detectCredentialProtectedWhileLocked()
        }
        if (Build.VERSION.SDK_INT >= 31) {
          detectIncorrectContextUse()
          detectUnsafeIntentLaunch()
        }
      }
      .penaltyLog()
      .build(),
  )
}
