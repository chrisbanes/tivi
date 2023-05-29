// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.ui

import app.tivi.common.ui.resources.MR
import app.tivi.data.models.Genre
import dev.icerock.moko.resources.StringResource

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

    fun getLabel(genre: Genre): StringResource = when (genre) {
        Genre.DRAMA -> MR.strings.genre_label_drama
        Genre.FANTASY -> MR.strings.genre_label_fantasy
        Genre.SCIENCE_FICTION -> MR.strings.genre_label_science_fiction
        Genre.ACTION -> MR.strings.genre_label_action
        Genre.ADVENTURE -> MR.strings.genre_label_adventure
        Genre.CRIME -> MR.strings.genre_label_crime
        Genre.THRILLER -> MR.strings.genre_label_thriller
        Genre.COMEDY -> MR.strings.genre_label_comedy
        Genre.HORROR -> MR.strings.genre_label_horror
        Genre.MYSTERY -> MR.strings.genre_label_mystery
    }
}
