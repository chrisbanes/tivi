// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.models

data class Season(
    override val id: Long = 0,
    val showId: Long,
    override val traktId: Int? = null,
    override val tmdbId: Int? = null,
    val title: String? = null,
    val summary: String? = null,
    val number: Int? = null,
    val network: String? = null,
    val episodeCount: Int? = null,
    val episodesAired: Int? = null,
    val traktRating: Float? = null,
    val traktRatingVotes: Int? = null,
    val tmdbPosterPath: String? = null,
    val tmdbBackdropPath: String? = null,
    val ignored: Boolean = false,
) : TiviEntity, TmdbIdEntity, TraktIdEntity {
    companion object {
        const val NUMBER_SPECIALS = 0
        val EMPTY = Season(showId = 0)
    }
}
