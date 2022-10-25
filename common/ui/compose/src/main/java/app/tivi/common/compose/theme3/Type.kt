/*
 * Copyright 2022 Google LLC
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

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import app.tivi.common.compose.theme.InterFontFamily

@OptIn(ExperimentalTextApi::class)
val TiviTypography by lazy {
    // Eugh, this is gross but there is no defaultFontFamily property in M3
    val default = Typography()
    Typography(
        displayLarge = default.displayLarge.copy(fontFamily = InterFontFamily),
        displayMedium = default.displayMedium.copy(fontFamily = InterFontFamily),
        displaySmall = default.displaySmall.copy(fontFamily = InterFontFamily),
        headlineLarge = default.headlineLarge.copy(fontFamily = InterFontFamily),
        headlineMedium = default.headlineMedium.copy(fontFamily = InterFontFamily),
        headlineSmall = default.headlineSmall.copy(fontFamily = InterFontFamily),
        titleLarge = default.titleLarge.copy(fontFamily = InterFontFamily),
        titleMedium = default.titleMedium.copy(fontFamily = InterFontFamily),
        titleSmall = default.titleSmall.copy(fontFamily = InterFontFamily),
        bodyLarge = default.bodyLarge.copy(fontFamily = InterFontFamily),
        bodyMedium = default.bodyMedium.copy(fontFamily = InterFontFamily),
        bodySmall = default.bodySmall.copy(fontFamily = InterFontFamily),
        labelLarge = default.labelLarge.copy(fontFamily = InterFontFamily),
        labelMedium = default.labelMedium.copy(fontFamily = InterFontFamily),
        labelSmall = default.labelSmall.copy(fontFamily = InterFontFamily)
    )
}
