// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.tasks

import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Provides

@OptIn(ExperimentalMultiplatform::class)
@AllowDifferentMembersInActual
actual interface TasksPlatformComponent {
  @ApplicationScope
  @Provides
  fun provideShowTasks(): ShowTasks = EmptyShowTasks
}

object EmptyShowTasks : ShowTasks {
  override fun register() = Unit
}
