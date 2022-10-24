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

package app.tivi.common.compose.theme3

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import app.tivi.common.compose.theme.Orange500
import app.tivi.common.compose.theme.Orange700
import app.tivi.common.compose.theme.Slate200
import app.tivi.common.compose.theme.Slate800

val TiviLightColors = lightColorScheme(
    primary = Slate800,
    onPrimary = Color.White,
    secondary = Orange700,
    onSecondary = Color.Black
)

val TiviDarkColors = darkColorScheme(
    primary = Slate200,
    onPrimary = Color.Black,
    secondary = Orange500,
    onSecondary = Color.Black
)
