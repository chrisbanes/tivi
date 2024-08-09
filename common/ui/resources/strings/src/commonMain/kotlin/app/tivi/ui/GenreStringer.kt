// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.ui

import app.tivi.common.ui.resources.strings.Res
import app.tivi.common.ui.resources.strings.genreLabelAction
import app.tivi.common.ui.resources.strings.genreLabelAdventure
import app.tivi.common.ui.resources.strings.genreLabelComedy
import app.tivi.common.ui.resources.strings.genreLabelCrime
import app.tivi.common.ui.resources.strings.genreLabelDrama
import app.tivi.common.ui.resources.strings.genreLabelFantasy
import app.tivi.common.ui.resources.strings.genreLabelHorror
import app.tivi.common.ui.resources.strings.genreLabelMystery
import app.tivi.common.ui.resources.strings.genreLabelScienceFiction
import app.tivi.common.ui.resources.strings.genreLabelThriller
import app.tivi.data.models.Genre
import org.jetbrains.compose.resources.StringResource

object GenreStringer {
  fun getEmoji(genre: Genre): String = when (genre) {
    Genre.DRAMA -> "\uD83D\uDE28"
    Genre.FANTASY -> "\uD83E\uDDD9"
    Genre.SCIENCE_FICTION -> "\uD83D\uDE80️"
    Genre.ACTION -> "\uD83E\uDD20"
    Genre.ADVENTURE -> "\uD83C\uDFDE️"
    Genre.CRIME -> "\uD83D\uDC6E"
    Genre.THRILLER -> "\uD83D\uDDE1️"
    Genre.COMEDY -> "\uD83E\uDD23"
    Genre.HORROR -> "\uD83E\uDDDF"
    Genre.MYSTERY -> "\uD83D\uDD75️"
  }
}

internal fun getGenreLabel(genre: Genre): StringResource = when (genre) {
  Genre.DRAMA -> Res.string.genreLabelDrama
  Genre.FANTASY -> Res.string.genreLabelFantasy
  Genre.SCIENCE_FICTION -> Res.string.genreLabelScienceFiction
  Genre.ACTION -> Res.string.genreLabelAction
  Genre.ADVENTURE -> Res.string.genreLabelAdventure
  Genre.CRIME -> Res.string.genreLabelCrime
  Genre.THRILLER -> Res.string.genreLabelThriller
  Genre.COMEDY -> Res.string.genreLabelComedy
  Genre.HORROR -> Res.string.genreLabelHorror
  Genre.MYSTERY -> Res.string.genreLabelMystery
}
