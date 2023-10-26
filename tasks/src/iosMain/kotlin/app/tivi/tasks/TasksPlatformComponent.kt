// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.tasks

import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Provides

actual interface TasksPlatformComponent {
  @ApplicationScope
  @Provides
  fun provideShowTasks(bind: IosShowTasks): ShowTasks = bind
}
