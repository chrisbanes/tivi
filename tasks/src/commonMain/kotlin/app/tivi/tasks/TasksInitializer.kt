// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.tasks

import app.tivi.appinitializers.AppInitializer
import app.tivi.inject.ApplicationCoroutineScope
import app.tivi.settings.TiviPreferences
import app.tivi.util.launchOrThrow
import me.tatarka.inject.annotations.Inject

@Inject
class TasksInitializer(
  tasks: Lazy<Tasks>,
  preferences: Lazy<TiviPreferences>,
  private val coroutineScope: ApplicationCoroutineScope,
) : AppInitializer {
  private val tasks by tasks
  private val preferences by preferences

  override fun initialize() {
    tasks.setup()

    tasks.scheduleLibrarySync()

    coroutineScope.launchOrThrow {
      preferences.episodeAiringNotificationsEnabled.flow
        .collect { enabled ->
          when {
            enabled -> tasks.scheduleEpisodeNotifications()
            else -> tasks.cancelEpisodeNotifications()
          }
        }
    }
  }
}
