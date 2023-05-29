// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.models

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class Episode(
    override val id: Long = 0,
    val seasonId: Long,
    override val traktId: Int? = null,
    override val tmdbId: Int? = null,
    val title: String? = null,
    val summary: String? = null,
    val number: Int? = null,
    val firstAired: Instant? = null,
    val traktRating: Float? = null,
    val traktRatingVotes: Int? = null,
    val tmdbBackdropPath: String? = null,
) : TiviEntity, TraktIdEntity, TmdbIdEntity {
    companion object {
        val EMPTY = Episode(seasonId = 0)
    }

    val hasAired: Boolean get() = firstAired?.let { it < Clock.System.now() } ?: false
}
