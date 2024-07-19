// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings

interface TiviPreferences {

  val theme: Preference<Theme>
  val useDynamicColors: Preference<Boolean>

  val useLessData: Preference<Boolean>

  val libraryFollowedActive: Preference<Boolean>

  val upNextFollowedOnly: Preference<Boolean>

  val ignoreSpecials: Preference<Boolean>
  val reportAppCrashes: Preference<Boolean>
  val reportAnalytics: Preference<Boolean>

  val developerHideArtwork: Preference<Boolean>

  val episodeAiringNotificationsEnabled: Preference<Boolean>

  enum class Theme {
    LIGHT,
    DARK,
    SYSTEM,
  }
}
