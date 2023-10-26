// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import app.tivi.appinitializers.AppInitializer
import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

actual interface LoggerPlatformComponent {

  @get:Provides
  val setCrashReportingEnabledAction: SetCrashReportingEnabledAction

  @Provides
  @ApplicationScope
  fun provideLogger(
    kermitLogger: KermitLogger,
    recordingLogger: RecordingLogger,
  ): Logger = CompositeLogger(kermitLogger, recordingLogger, CrashKIosLogger)

  @Provides
  @IntoSet
  fun provideCrashKiOSInitializer(): AppInitializer = IosCrashKiOSInitializer
}
