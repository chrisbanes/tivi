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

package app.tivi.data.mappers

import app.tivi.data.entities.TiviShow
import com.uwetrottmann.trakt5.entities.Show
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraktShowToTiviShow @Inject constructor(
    private val statusMapper: TraktStatusToShowStatus
) : Mapper<Show, TiviShow> {
    override suspend fun map(from: Show) = TiviShow(
        traktId = from.ids?.trakt,
        tmdbId = from.ids?.tmdb,
        imdbId = from.ids?.imdb,
        title = from.title,
        summary = from.overview,
        homepage = from.homepage,
        traktRating = from.rating?.toFloat(),
        traktVotes = from.votes,
        certification = from.certification,
        runtime = from.runtime,
        network = from.network,
        country = from.country,
        firstAired = from.first_aired,
        _genres = from.genres?.joinToString(","),
        traktDataUpdate = from.updated_at,
        status = from.status?.let {
            statusMapper.map(it)
        },
        airsDay = from.airs?.day?.let { dayString ->
            DayOfWeek.values().firstOrNull { day ->
                dayString.equals(day.getDisplayName(TextStyle.FULL, Locale.getDefault()), true)
            }
        },
        airsTime = from.airs?.time?.let {
            try {
                LocalTime.parse(it)
            } catch (e: Exception) {
                null
            }
        },
        airsTimeZone = from.airs?.timezone?.let {
            try {
                ZoneId.of(it)
            } catch (e: Exception) {
                null
            }
        }
    )
}
