// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.analytics

import app.tivi.appinitializers.AppInitializer
import app.tivi.inject.ApplicationCoroutineScope
import app.tivi.settings.TiviPreferences
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class AnalyticsInitializer(
  private val preferences: Lazy<TiviPreferences>,
  private val analytics: Lazy<Analytics>,
  private val scope: ApplicationCoroutineScope,
) : AppInitializer {
  override fun initialize() {
    scope.launch {
      preferences.value.reportAnalytics.flow
        .collect { enabled -> analytics.value.setEnabled(enabled) }
    }
  }
}
