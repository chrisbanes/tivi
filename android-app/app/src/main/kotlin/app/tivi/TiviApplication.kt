// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi

import android.app.Application
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
    super.onCreate()

    workerFactory = component.workerFactory

    component.initializers.initialize()
  }

  override val workManagerConfiguration: Configuration
    get() = Configuration.Builder()
      .setWorkerFactory(workerFactory)
      .build()
}
