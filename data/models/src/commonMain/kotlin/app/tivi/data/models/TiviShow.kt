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
