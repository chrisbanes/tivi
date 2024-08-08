// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import app.tivi.app.ApplicationInfo
import app.tivi.app.Flavor
import app.tivi.appinitializers.AppInitializer
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import me.tatarka.inject.annotations.Inject

@Inject
class KermitInitializer(
  private val applicationInfo: ApplicationInfo,
) : AppInitializer {
  override fun initialize() {
    Logger.setMinSeverity(
      when {
        applicationInfo.debugBuild -> Severity.Debug
        applicationInfo.flavor == Flavor.Qa -> Severity.Debug
        else -> Severity.Error
      },
    )

    if (applicationInfo.debugBuild || applicationInfo.flavor == Flavor.Qa) {
      Logger.addLogWriter(RecordingLoggerWriter)
    }
  }
}
