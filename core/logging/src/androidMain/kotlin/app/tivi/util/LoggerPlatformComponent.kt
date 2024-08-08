// Copyright 2022, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import app.tivi.appinitializers.AppInitializer
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

actual interface LoggerPlatformComponent {
  @Provides
  @IntoSet
  fun provideCrashlyticsAndroidInitializer(): AppInitializer = CrashlyticsAndroidInitializer

  @Provides
  fun bindSetCrashReportingEnabledAction(): SetCrashReportingEnabledAction {
    return AndroidSetCrashReportingEnabledAction
  }
}
