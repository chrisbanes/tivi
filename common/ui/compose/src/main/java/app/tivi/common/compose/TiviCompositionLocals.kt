// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose

import androidx.compose.runtime.staticCompositionLocalOf
import app.tivi.util.TiviDateFormatter
import app.tivi.util.TiviTextCreator

val LocalTiviDateFormatter = staticCompositionLocalOf<TiviDateFormatter> {
    error("TiviDateFormatter not provided")
}

val LocalTiviTextCreator = staticCompositionLocalOf<TiviTextCreator> {
    error("TiviTextCreator not provided")
}
