// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import app.tivi.appinitializers.AppInitializer
import app.tivi.inject.ApplicationCoroutineScope
import app.tivi.settings.TiviPreferences
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class CrashReportingInitializer(
  private val preferences: Lazy<TiviPreferences>,
  private val action: Lazy<SetCrashReportingEnabledAction>,
  private val scope: ApplicationCoroutineScope,
) : AppInitializer {
  override fun initialize() {
    scope.launch {
      preferences.value.reportAppCrashes.flow
        .collect { action.value(it) }
    }
  }
}
