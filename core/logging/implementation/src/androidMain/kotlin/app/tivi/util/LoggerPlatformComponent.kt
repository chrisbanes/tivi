// Copyright 2022, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import app.tivi.appinitializers.AppInitializer
import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

actual interface LoggerPlatformComponent {
  @ApplicationScope
  @Provides
  fun provideLogger(
    timberLogger: TimberLogger,
    recordingLogger: RecordingLogger,
  ): Logger = CompositeLogger(timberLogger, recordingLogger)

  @Provides
  @IntoSet
  fun provideCrashKiOSInitializer(): AppInitializer = AndroidCrashKiOSInitializer

  @Provides
  fun bindSetCrashReportingEnabledAction(): SetCrashReportingEnabledAction {
    return AndroidSetCrashReportingEnabledAction
  }
}
