// Copyright 2022, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.theme

import androidx.compose.runtime.Composable
import app.tivi.common.ui.resources.DmSansFontFamily

internal val TiviMaterialTypography: androidx.compose.material3.Typography
  @Composable get() {
    // Eugh, this is gross but there is no defaultFontFamily property in M3
    val default = androidx.compose.material3.Typography()
    val fontFamily = DmSansFontFamily
    return androidx.compose.material3.Typography(
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

internal val TiviCupertinoTypography: io.github.alexzhirkevich.cupertino.theme.Typography
  @Composable get() {
    val default = io.github.alexzhirkevich.cupertino.theme.Typography()
    val fontFamily = DmSansFontFamily
    return io.github.alexzhirkevich.cupertino.theme.Typography(
      largeTitle = default.largeTitle.copy(fontFamily = fontFamily),
      title1 = default.title1.copy(fontFamily = fontFamily),
      title2 = default.title2.copy(fontFamily = fontFamily),
      title3 = default.title3.copy(fontFamily = fontFamily),
      headline = default.headline.copy(fontFamily = fontFamily),
      body = default.body.copy(fontFamily = fontFamily),
      callout = default.callout.copy(fontFamily = fontFamily),
      subhead = default.subhead.copy(fontFamily = fontFamily),
      footnote = default.footnote.copy(fontFamily = fontFamily),
      caption1 = default.caption1.copy(fontFamily = fontFamily),
      caption2 = default.caption2.copy(fontFamily = fontFamily),
    )
  }
