// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import app.tivi.appinitializers.AppInitializer
import co.touchlab.crashkios.crashlytics.enableCrashlytics

internal object AndroidCrashKiOSInitializer : AppInitializer {
  override fun initialize() {
    enableCrashlytics()
  }
}
