/*
 * Copyright 2017 Google LLC
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

package app.tivi.ui

import androidx.annotation.StringRes
import app.tivi.data.entities.Genre
import app.tivi.common.ui.resources.R as UiR

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

    @StringRes
    fun getLabel(genre: Genre): Int = when (genre) {
        Genre.DRAMA -> UiR.string.genre_label_drama
        Genre.FANTASY -> UiR.string.genre_label_fantasy
        Genre.SCIENCE_FICTION -> UiR.string.genre_label_science_fiction
        Genre.ACTION -> UiR.string.genre_label_action
        Genre.ADVENTURE -> UiR.string.genre_label_adventure
        Genre.CRIME -> UiR.string.genre_label_crime
        Genre.THRILLER -> UiR.string.genre_label_thriller
        Genre.COMEDY -> UiR.string.genre_label_comedy
        Genre.HORROR -> UiR.string.genre_label_horror
        Genre.MYSTERY -> UiR.string.genre_label_mystery
    }
}
