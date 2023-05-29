// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

internal val Slate200 = Color(0xFF81A9B3)
internal val Slate600 = Color(0xFF4A6572)
internal val Slate800 = Color(0xFF232F34)

internal val Orange500 = Color(0xFFF9AA33)
internal val Orange700 = Color(0xFFC78522)

val TiviLightColors = lightColorScheme(
    primary = Slate800,
    onPrimary = Color.White,
    secondary = Orange700,
    onSecondary = Color.Black,
)

val TiviDarkColors = darkColorScheme(
    primary = Slate200,
    onPrimary = Color.Black,
    secondary = Orange500,
    onSecondary = Color.Black,
)
