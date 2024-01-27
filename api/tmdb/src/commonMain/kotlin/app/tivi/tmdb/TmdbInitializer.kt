// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.tmdb

import app.tivi.appinitializers.AppInitializer
import app.tivi.inject.ApplicationCoroutineScope
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class TmdbInitializer(
  private val tmdbManager: Lazy<TmdbManager>,
  private val scope: ApplicationCoroutineScope,
) : AppInitializer {
  override fun initialize() {
    scope.launch {
      tmdbManager.value.refreshConfiguration()
    }
  }
}
