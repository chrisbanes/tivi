// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.tasks

import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Provides

actual interface TasksPlatformComponent {
  @ApplicationScope
  @Provides
  fun provideShowTasks(): Tasks = EmptyShowTasks
}

object EmptyShowTasks : Tasks {
  override fun scheduleEpisodeNotifications() = Unit
  override fun cancelEpisodeNotifications() = Unit
  override fun scheduleLibrarySync() = Unit
  override fun cancelLibrarySync() = Unit
}
