// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import app.tivi.common.ui.resources.EnTiviStrings
import app.tivi.common.ui.resources.Strings
import app.tivi.common.ui.resources.TiviStrings
import cafe.adriel.lyricist.LanguageTag
import cafe.adriel.lyricist.Lyricist
import cafe.adriel.lyricist.ProvideStrings
import cafe.adriel.lyricist.rememberStrings

val LocalStrings: ProvidableCompositionLocal<TiviStrings> = compositionLocalOf { EnTiviStrings }

@Composable
fun rememberStrings(
  languageTag: LanguageTag = "en",
): Lyricist<TiviStrings> = rememberStrings(Strings, languageTag)

@Composable
fun ProvideStrings(
  lyricist: Lyricist<TiviStrings> = rememberStrings(),
  content: @Composable () -> Unit,
) {
  ProvideStrings(lyricist, LocalStrings, content)
}
