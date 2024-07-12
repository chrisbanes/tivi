// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose

import androidx.compose.runtime.staticCompositionLocalOf
import app.tivi.settings.TiviPreferences
import app.tivi.util.TiviDateFormatter
import app.tivi.util.TiviTextCreator

val LocalTiviDateFormatter = staticCompositionLocalOf<TiviDateFormatter> {
  error("TiviDateFormatter not provided")
}

val LocalTiviTextCreator = staticCompositionLocalOf<TiviTextCreator> {
  error("TiviTextCreator not provided")
}

val LocalColorExtractor = staticCompositionLocalOf<ColorExtractor> {
  error("LocalColorExtractor not provided")
}

val LocalPreferences = staticCompositionLocalOf<TiviPreferences> {
  error("LocalPreferences not provided")
}
