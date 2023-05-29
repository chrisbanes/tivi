// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.tmdb

object TmdbImageSizes {

    const val baseImageUrl = "https://image.tmdb.org/t/p/"

    val posterSizes = listOf(
        "w92",
        "w154",
        "w185",
        "w342",
        "w500",
        "w780",
        "original",
    )

    val backdropSizes = listOf(
        "w300",
        "w780",
        "w1280",
        "original",
    )

    val logoSizes = listOf(
        "w45",
        "w92",
        "w154",
        "w185",
        "w300",
        "w500",
        "original",
    )
}
