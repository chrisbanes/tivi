/*
 * Copyright 2018 Google LLC
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
