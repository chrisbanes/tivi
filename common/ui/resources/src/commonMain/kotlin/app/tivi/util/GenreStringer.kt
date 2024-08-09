// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import app.tivi.common.ui.resources.Res
import app.tivi.common.ui.resources.genre_label_action
import app.tivi.common.ui.resources.genre_label_adventure
import app.tivi.common.ui.resources.genre_label_comedy
import app.tivi.common.ui.resources.genre_label_crime
import app.tivi.common.ui.resources.genre_label_drama
import app.tivi.common.ui.resources.genre_label_fantasy
import app.tivi.common.ui.resources.genre_label_horror
import app.tivi.common.ui.resources.genre_label_mystery
import app.tivi.common.ui.resources.genre_label_science_fiction
import app.tivi.common.ui.resources.genre_label_thriller
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
  Genre.DRAMA -> Res.string.genre_label_drama
  Genre.FANTASY -> Res.string.genre_label_fantasy
  Genre.SCIENCE_FICTION -> Res.string.genre_label_science_fiction
  Genre.ACTION -> Res.string.genre_label_action
  Genre.ADVENTURE -> Res.string.genre_label_adventure
  Genre.CRIME -> Res.string.genre_label_crime
  Genre.THRILLER -> Res.string.genre_label_thriller
  Genre.COMEDY -> Res.string.genre_label_comedy
  Genre.HORROR -> Res.string.genre_label_horror
  Genre.MYSTERY -> Res.string.genre_label_mystery
}
