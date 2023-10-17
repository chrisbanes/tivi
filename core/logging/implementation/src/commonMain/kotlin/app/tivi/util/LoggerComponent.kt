// Copyright 2022, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import app.tivi.app.ApplicationInfo
import app.tivi.app.Flavor
import app.tivi.appinitializers.AppInitializer
import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

expect interface LoggerPlatformComponent

interface LoggerComponent : LoggerPlatformComponent {
  @ApplicationScope
  @Provides
  fun bindRecordingLogger(
    applicationInfo: ApplicationInfo,
  ): RecordingLogger = when {
    applicationInfo.debugBuild -> RecordingLoggerImpl()
    applicationInfo.flavor == Flavor.Qa -> RecordingLoggerImpl()
    else -> NoopRecordingLogger
  }

  @Provides
  @IntoSet
  fun provideCrashReportingInitializer(impl: CrashReportingInitializer): AppInitializer = impl
}
