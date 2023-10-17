// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import app.tivi.appinitializers.AppInitializer
import co.touchlab.crashkios.crashlytics.enableCrashlytics
import co.touchlab.crashkios.crashlytics.setCrashlyticsUnhandledExceptionHook

internal object IosCrashKiOSInitializer : AppInitializer {
  override fun initialize() {
    // https://crashkios.touchlab.co/docs/crashlytics#step-2---add-crashkios
    enableCrashlytics()
    setCrashlyticsUnhandledExceptionHook()
  }
}
