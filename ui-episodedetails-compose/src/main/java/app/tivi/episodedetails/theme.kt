/*
 * Copyright 2019 Google LLC
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

package app.tivi.episodedetails

import androidx.ui.graphics.Color
import androidx.ui.material.ColorPalette

val lightThemeColors = ColorPalette(
        primary = Color(0xFFDD0D3C),
        primaryVariant = Color(0xFFC20029),
        onPrimary = Color.White,
        secondary = Color.White,
        onSecondary = Color.Black,
        background = Color.White,
        onBackground = Color.Black,
        surface = Color.White,
        onSurface = Color.Black,
        error = Color(0xFFD00036),
        onError = Color.White
)

val darkThemeColors = ColorPalette(
        primary = Color(0xFFEA6D7E),
        primaryVariant = Color(0xFFDD0D3E),
        onPrimary = Color.Black,
        secondary = Color(0xFF121212),
        onSecondary = Color.White,
        background = Color(0xFF121212),
        onBackground = Color.White,
        surface = Color(0xFF121212),
        onSurface = Color.White
)
