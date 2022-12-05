/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
