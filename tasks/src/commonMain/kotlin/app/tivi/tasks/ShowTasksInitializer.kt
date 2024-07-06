// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.tasks

import app.tivi.appinitializers.AppInitializer
import me.tatarka.inject.annotations.Inject

@Inject
class ShowTasksInitializer(
  showTasks: Lazy<ShowTasks>,
) : AppInitializer {
  private val showTasks by showTasks

  override fun initialize() {
    showTasks.registerPeriodicTasks()
    showTasks.enqueueStartupTasks()
  }
}
