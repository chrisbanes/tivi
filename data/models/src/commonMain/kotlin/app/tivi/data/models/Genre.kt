// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.models

/* ktlint-disable trailing-comma-on-declaration-site */
enum class Genre(val traktValue: String) {
    DRAMA("drama"),
    FANTASY("fantasy"),
    SCIENCE_FICTION("science-fiction"),
    ACTION("action"),
    ADVENTURE("adventure"),
    CRIME("crime"),
    THRILLER("thriller"),
    COMEDY("comedy"),
    HORROR("horror"),
    MYSTERY("mystery");

    companion object {
        private val values by lazy { values() }
        fun fromTraktValue(value: String) = values.firstOrNull { it.traktValue == value }
    }
}
