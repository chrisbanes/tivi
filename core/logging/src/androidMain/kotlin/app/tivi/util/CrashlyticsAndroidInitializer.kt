// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import app.tivi.appinitializers.AppInitializer
import co.touchlab.crashkios.crashlytics.enableCrashlytics
import co.touchlab.kermit.Logger

internal object CrashlyticsAndroidInitializer : AppInitializer {
  override fun initialize() {
    enableCrashlytics()

    // Add Crashlytics log writer
    Logger.addLogWriter(CrashlyticsLoggerWriter())
  }
}
