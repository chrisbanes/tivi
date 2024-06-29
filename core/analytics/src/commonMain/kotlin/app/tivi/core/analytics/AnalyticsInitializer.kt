// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.core.analytics

import app.tivi.appinitializers.AppInitializer
import app.tivi.inject.ApplicationCoroutineScope
import app.tivi.settings.TiviPreferences
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class AnalyticsInitializer(
  private val preferences: Lazy<TiviPreferences>,
  private val scope: ApplicationCoroutineScope,
  private val analytics: Analytics,
  private val dispatchers: AppCoroutineDispatchers,
) : AppInitializer {
  override fun initialize() {
    scope.launch {
      preferences.value.reportAnalytics.flow
        .flowOn(dispatchers.io)
        .collect { enabled -> analytics.setEnabled(enabled) }
    }
  }
}
