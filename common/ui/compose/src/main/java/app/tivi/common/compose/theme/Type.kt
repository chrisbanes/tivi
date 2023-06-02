// Copyright 2022, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import app.tivi.common.ui.resources.MR
import dev.icerock.moko.resources.compose.asFont

internal val InterFontFamily: FontFamily
    @Composable get() = FontFamily(
        MR.fonts.Inter.light.asFont(FontWeight.Light)!!,
        MR.fonts.Inter.regular.asFont(FontWeight.Normal)!!,
        MR.fonts.Inter.medium.asFont(FontWeight.Medium)!!,
        MR.fonts.Inter.bold.asFont(FontWeight.Bold)!!,
    )

val TiviTypography: Typography
    @Composable get() {
        // Eugh, this is gross but there is no defaultFontFamily property in M3
        val default = Typography()
        val fontFamily = InterFontFamily
        return Typography(
            displayLarge = default.displayLarge.copy(fontFamily = fontFamily),
            displayMedium = default.displayMedium.copy(fontFamily = fontFamily),
            displaySmall = default.displaySmall.copy(fontFamily = fontFamily),
            headlineLarge = default.headlineLarge.copy(fontFamily = fontFamily),
            headlineMedium = default.headlineMedium.copy(fontFamily = fontFamily),
            headlineSmall = default.headlineSmall.copy(fontFamily = fontFamily),
            titleLarge = default.titleLarge.copy(fontFamily = fontFamily),
            titleMedium = default.titleMedium.copy(fontFamily = fontFamily),
            titleSmall = default.titleSmall.copy(fontFamily = fontFamily),
            bodyLarge = default.bodyLarge.copy(fontFamily = fontFamily),
            bodyMedium = default.bodyMedium.copy(fontFamily = fontFamily),
            bodySmall = default.bodySmall.copy(fontFamily = fontFamily),
            labelLarge = default.labelLarge.copy(fontFamily = fontFamily),
            labelMedium = default.labelMedium.copy(fontFamily = fontFamily),
            labelSmall = default.labelSmall.copy(fontFamily = fontFamily),
        )
    }
