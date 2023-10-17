// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.ui

import app.tivi.common.ui.resources.TiviStrings
import app.tivi.data.models.Genre

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

internal fun TiviStrings.getGenreLabel(genre: Genre): String = when (genre) {
  Genre.DRAMA -> genreLabelDrama
  Genre.FANTASY -> genreLabelFantasy
  Genre.SCIENCE_FICTION -> genreLabelScienceFiction
  Genre.ACTION -> genreLabelAction
  Genre.ADVENTURE -> genreLabelAdventure
  Genre.CRIME -> genreLabelCrime
  Genre.THRILLER -> genreLabelThriller
  Genre.COMEDY -> genreLabelComedy
  Genre.HORROR -> genreLabelHorror
  Genre.MYSTERY -> genreLabelMystery
}
