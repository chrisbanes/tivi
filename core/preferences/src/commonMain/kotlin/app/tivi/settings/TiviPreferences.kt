// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings

import kotlin.reflect.KMutableProperty0
import kotlinx.coroutines.flow.Flow

interface TiviPreferences {

  var theme: Theme
  fun observeTheme(): Flow<Theme>

  var useDynamicColors: Boolean
  fun observeUseDynamicColors(): Flow<Boolean>

  var useLessData: Boolean
  fun observeUseLessData(): Flow<Boolean>

  var libraryFollowedActive: Boolean
  fun observeLibraryFollowedActive(): Flow<Boolean>

  var libraryWatchedActive: Boolean
  fun observeLibraryWatchedActive(): Flow<Boolean>

  var upNextFollowedOnly: Boolean
  fun observeUpNextFollowedOnly(): Flow<Boolean>

  var ignoreSpecials: Boolean
  fun observeIgnoreSpecials(): Flow<Boolean>

  var reportAppCrashes: Boolean
  fun observeReportAppCrashes(): Flow<Boolean>

  var reportAnalytics: Boolean
  fun observeReportAnalytics(): Flow<Boolean>

  var developerHideArtwork: Boolean
  fun observeDeveloperHideArtwork(): Flow<Boolean>

  enum class Theme {
    LIGHT,
    DARK,
    SYSTEM,
  }
}

fun KMutableProperty0<Boolean>.toggle() {
  set(!get())
}
