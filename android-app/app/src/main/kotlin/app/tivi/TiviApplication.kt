// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import androidx.work.Configuration
import androidx.work.WorkerFactory
import app.tivi.extensions.unsafeLazy
import app.tivi.inject.AndroidApplicationComponent
import app.tivi.inject.create

class TiviApplication : Application(), Configuration.Provider {
  val component: AndroidApplicationComponent by unsafeLazy {
    AndroidApplicationComponent.create(this)
  }

  private lateinit var workerFactory: WorkerFactory

  override fun onCreate() {
    StrictMode.setThreadPolicy(
      ThreadPolicy.Builder()
        .detectAll()
        .penaltyLog()
        .build(),
    )
    StrictMode.setVmPolicy(
      VmPolicy.Builder()
        .detectAll()
        .penaltyLog()
        .build(),
    )

    super.onCreate()

    workerFactory = component.workerFactory

    component.initializers.initialize()
  }

  override val workManagerConfiguration: Configuration
    get() = Configuration.Builder()
      .setWorkerFactory(workerFactory)
      .build()
}
