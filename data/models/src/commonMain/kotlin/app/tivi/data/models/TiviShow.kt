// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.models

import app.tivi.extensions.unsafeLazy
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone

data class TiviShow(
    override val id: Long = 0,
    val title: String? = null,
    val originalTitle: String? = null,
    override val traktId: Int? = null,
    override val tmdbId: Int? = null,
    val imdbId: String? = null,
    val summary: String? = null,
    val homepage: String? = null,
    val traktRating: Float? = null,
    val traktVotes: Int? = null,
    val certification: String? = null,
    val firstAired: Instant? = null,
    val country: String? = null,
    val network: String? = null,
    val networkLogoPath: String? = null,
    val runtime: Int? = null,
    val _genres: String? = null,
    val status: ShowStatus? = null,
    val airsDay: DayOfWeek? = null,
    val airsTime: LocalTime? = null,
    val airsTimeZone: TimeZone? = null,
) : TiviEntity, TraktIdEntity, TmdbIdEntity {
    constructor() : this(0)

    val genres by unsafeLazy {
        _genres?.split(",")?.mapNotNull { Genre.fromTraktValue(it.trim()) } ?: emptyList()
    }

    companion object {
        val EMPTY_SHOW = TiviShow()
    }
}
