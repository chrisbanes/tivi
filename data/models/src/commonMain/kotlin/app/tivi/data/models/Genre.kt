// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

@file:Suppress("ktlint:standard:trailing-comma-on-declaration-site")

package app.tivi.data.models

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
        fun fromTraktValue(value: String): Genre? = Genre.entries.firstOrNull {
            it.traktValue == value
        }
    }
}
