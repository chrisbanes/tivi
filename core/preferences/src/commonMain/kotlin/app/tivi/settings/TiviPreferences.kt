// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings

import kotlinx.coroutines.flow.Flow

interface TiviPreferences {
  suspend fun setTheme(theme: Theme)
  fun observeTheme(): Flow<Theme>

  suspend fun toggleUseDynamicColors()

  fun observeUseDynamicColors(): Flow<Boolean>

  suspend fun getUseLessData(): Boolean

  suspend fun toggleUseLessData()

  fun observeUseLessData(): Flow<Boolean>

  suspend fun toggleLibraryFollowedActive()

  fun observeLibraryFollowedActive(): Flow<Boolean>

  suspend fun toggleLibraryWatchedActive()

  fun observeLibraryWatchedActive(): Flow<Boolean>

  suspend fun toggleUpNextFollowedOnly()

  fun observeUpNextFollowedOnly(): Flow<Boolean>

  suspend fun toggleIgnoreSpecials()
  fun observeIgnoreSpecials(): Flow<Boolean>

  suspend fun toggleReportAppCrashes()
  fun observeReportAppCrashes(): Flow<Boolean>

  suspend fun toggleReportAnalytics()
  fun observeReportAnalytics(): Flow<Boolean>

  suspend fun toggleDeveloperHideArtwork()
  suspend fun getDeveloperHideArtwork(): Boolean
  fun observeDeveloperHideArtwork(): Flow<Boolean>

  enum class Theme {
    LIGHT,
    DARK,
    SYSTEM,
  }
}
